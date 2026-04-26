# 消除星星 (Star Eliminator)

一款使用 Kotlin + Jetpack Compose 构建的 Android 休闲益智游戏。

## 玩法

点击 **2 个或以上** 相连的同色星星即可消除：

- **计分**：消除 N 个星星 = `5 × N × (N − 1)` 分
- **清空棋盘**：额外奖励 2000 分
- **连击**：连续消除触发倍率加成，最高 3.0×
- **关卡制**：每关有目标分数，达标进入下一关，未达标游戏结束
- **无限关卡**：目标分数随关卡递增，公式为 `200 + level² × 250`

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Canvas |
| 架构 | MVVM + Repository |
| 数据库 | Room |
| 音效 | SoundPool（程序化生成 WAV） |
| 动画 | 手动帧循环 + FastOutSlowIn 缓动 |

## 项目结构

```
app/src/main/java/com/example/stareliminator/
├── audio/
│   └── SoundManager.kt          # 音效管理（SoundPool + 程序化 WAV）
├── data/
│   ├── local/                   # Room 数据库、DAO、Entity
│   ├── model/                   # Cell 数据类
│   └── repository/              # GameRepository
├── domain/
│   ├── BoardGenerator.kt        # 棋盘生成器
│   ├── CellMove.kt              # 动画移动数据
│   ├── FloodFill.kt             # BFS 连通域检测
│   ├── GameEngine.kt            # 核心游戏逻辑
│   └── GravityCollapse.kt       # 重力下落 + 列塌缩
├── ui/
│   ├── components/              # 可复用组件（HUD、弹窗、星星渲染）
│   ├── navigation/              # 导航图
│   ├── screens/game/            # 游戏界面 + ViewModel + Canvas
│   ├── screens/highscore/       # 排行榜
│   ├── screens/menu/            # 主菜单
│   └── theme/                   # Material3 深色太空主题
└── util/
    ├── Constants.kt             # 游戏常量
    └── GridSerializer.kt        # 棋盘 JSON 序列化
```

## 构建与运行

1. 用 Android Studio 打开项目
2. 同步 Gradle
3. 在模拟器或真机上运行

最低 SDK：26（Android 8.0）
