# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 构建命令

```bash
# 编译 debug APK
./gradlew assembleDebug

# 编译 release APK（开启混淆）
./gradlew assembleRelease

# 运行单元测试（JVM，不依赖 Android 设备）
./gradlew test

# 运行单个测试类
./gradlew test --tests "com.example.stareliminator.domain.GameEngineTest"

# 运行单个测试方法
./gradlew test --tests "com.example.stareliminator.domain.GameEngineTest.testTapRemovesGroup"

# 安装到连接的设备/模拟器
./gradlew installDebug
```

APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`

## 技术栈

- **语言**：Kotlin 1.9.22
- **UI**：Jetpack Compose + Canvas 自绘（非 View 体系）
- **架构**：MVVM + Repository，ViewModel 通过手动 Factory 注入依赖
- **数据库**：Room 2.6.1（KSP 编译时注解处理）
- **导航**：Navigation Compose
- **最低 SDK**：24（Android 7.0），目标 SDK 34

## 核心架构

### 依赖流向

```
UI (Compose Canvas) → ViewModel → Repository → Room DAO
                    ↘ GameEngine (纯逻辑，无副作用)
                       ├── FloodFill (BFS 连通域)
                       ├── GravityCollapse (重力下落 + 列塌缩)
                       └── BoardGenerator (随机棋盘生成)
```

### 关键入口

- [MainActivity.kt](app/src/main/java/com/example/stareliminator/MainActivity.kt) — 唯一 Activity，setContent 直接进入 NavGraph
- [StarEliminatorApplication.kt](app/src/main/java/com/example/stareliminator/StarEliminatorApplication.kt) — Application 子类，持有 Room Database 和 SoundManager 单例
- [NavGraph.kt](app/src/main/java/com/example/stareliminator/ui/navigation/NavGraph.kt) — 三屏导航：Menu → Game → HighScores，ViewModel 在此层创建并传入子 Composable

### 领域层（domain/）

所有领域对象都是 Kotlin `object`（无状态单例）：

- [GameEngine.kt](app/src/main/java/com/example/stareliminator/domain/GameEngine.kt) — 核心游戏逻辑。`processTapWithAnimation` 返回 `MoveResult.AnimatedSuccessful`（含 gravity/collapse 移动轨迹），`processTap` 返回不含动画数据的简化结果。`hasValidMoves` 扫描全棋盘判断是否死局。
- [FloodFill.kt](app/src/main/java/com/example/stareliminator/domain/FloodFill.kt) — BFS 查找同色连通域，用于点击预览高亮和消除判定。
- [GravityCollapse.kt](app/src/main/java/com/example/stareliminator/domain/GravityCollapse.kt) — 先每列重力下落（星星沉底），再左移塌缩空列。`WithTracking` 变体记录每个格子的移动轨迹供动画使用。
- [BoardGenerator.kt](app/src/main/java/com/example/stareliminator/domain/BoardGenerator.kt) — 随机生成 10×10 棋盘，`generateWithValidMoves` 生成 200 次取最优，保证初始棋盘至少有 10 个可消除组。

### 数据层（data/）

- [GameRepository.kt](app/src/main/java/com/example/stareliminator/data/repository/GameRepository.kt) — 统一数据访问入口：存档（序列化棋盘为 JSON）、最高分持久化
- [AppDatabase.kt](app/src/main/java/com/example/stareliminator/data/local/AppDatabase.kt) — Room 数据库，`star_eliminator.db`，含 `SavedGameEntity` 和 `HighScoreEntity` 两张表
- [GridSerializer.kt](app/src/main/java/com/example/stareliminator/util/GridSerializer.kt) — `Array<IntArray>` ↔ JSON 字符串序列化

### ViewModel（ui/screens/game/）

[GameViewModel.kt](app/src/main/java/com/example/stareliminator/ui/screens/game/GameViewModel.kt) 管理全部游戏状态（`GameUiState`），包括：

- 动画状态机：`IDLE → GRAVITY → COLLAPSE → IDLE`，使用手动帧循环（`runAnimation`）而非 Compose Animatable
- 连击系统：连续消除累积 combo，倍率从 1.0× 递增至最高 3.0×
- 关卡系统：目标分数 = `200 + level² × 250`，达标进下一关，未达标结束
- 计分公式：`5 × N × (N−1)`（N 为消除数量），清空棋盘额外 2000 分

### UI 渲染（ui/screens/game/）

[GameCanvas.kt](app/src/main/java/com/example/stareliminator/ui/screens/game/GameCanvas.kt) 用 Compose Canvas 自绘 10×10 网格。动画期间分别处理：
- GRAVITY 阶段：被消除格子透明消失，其余格子插值从旧位置移动到 gravityGrid 位置
- COLLAPSE 阶段：列整体从 gravityGrid 位置插值移动到 finalGrid 位置

### 音效（audio/）

[SoundManager.kt](app/src/main/java/com/example/stareliminator/audio/SoundManager.kt) 使用 SoundPool + 程序化生成 WAV（方波/正弦波合成，不依赖音频文件），五种音效类型：ELIMINATE、COMBO、BOARD_CLEAR、GAME_OVER、INVALID_TAP。

## 测试

- 单元测试位于 `app/src/test/`，纯 JVM 测试（不依赖 Android 框架）
- 当前覆盖：`FloodFillTest`、`GravityCollapseTest`、`GameEngineTest`
- 无 Android instrumented tests（`app/src/androidTest/` 为空）

## 注意事项

- 棋盘使用 `Array<IntArray>`（10×10），0 表示空格，1-5 表示不同颜色星星
- `Array.equals()` 不比较内容 — 使用 `contentEquals` 或手动比较
- Room 存棋盘时通过 GridSerializer 将 `Array<IntArray>` 转为 JSON 字符串存储
- SoundManager 在 Application 层初始化，通过 NavGraph 传入 GameViewModel 的 Factory
- enableEdgeToEdge() 被注释掉以确保 Canvas 区域不被系统栏遮挡
