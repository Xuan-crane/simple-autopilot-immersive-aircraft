# 简单自动驾驶沉浸式飞机

**Simple Autopilot for Immersive Aircraft — 1.2.1 Alpha**

A client-side addon with independent automatic-forward (V) and terrain-relative altitude (H) controls. Page Up/Down adjusts clearance; steering remains manual. Supports the seven original Immersive Aircraft aircraft, including separate airplane pitch/throttle handling. Reads loaded terrain and scans nearby obstacles up to 48 blocks ahead.

Requires **Minecraft 1.21.1**, **NeoForge 21.1.x (minimum 21.1.1)** and **Immersive Aircraft 1.5.x (minimum 1.5.0)**. Install the addon on the client only; your server needs the original Immersive Aircraft, with the same version as your client. This package does not support Fabric or Forge.

Download the playable `.jar` from [Releases](https://github.com/Xuan-crane/simple-autopilot-immersive-aircraft/releases). Do not install the source archive. Remove the previous addon JAR when upgrading; the internal mod ID is still `aircraft_autoforward`.

Altitude assistance is experimental: 48 automated tests pass, but gameplay and multiplayer flight still need testing. This addon does not plan routes, automatically land, or guarantee collision avoidance. See [verification and manual testing](TESTING.md). On multiplayer servers, follow the server's rules for automated controls.

Source license: [GPL-3.0-only](LICENSE). Immersive Aircraft by **Conczin / Luke100000** is required separately and is not bundled. See [the original project](https://github.com/Luke100000/ImmersiveAircraft).

---

给 Immersive Aircraft 增加自动前进、按地形调整离地高度，以及前方方块预判。自动升降与避障为待实机调校的试用功能。

## 安装

使用 **Minecraft 1.21.1 + NeoForge 21.1.1 或更新的 21.1.x + Immersive Aircraft 1.5.x（最低 1.5.0）**。这是非官方客户端附属模组。

1. 正常安装上述游戏环境与原模组。原模组官方下载：[1.5.0（NeoForge 1.21.1）](https://modrinth.com/mod/immersive-aircraft/version/8YWxZfqK)、[1.5.2（NeoForge 1.21.1）](https://modrinth.com/mod/immersive-aircraft/version/ZZTlNkV9)。客户端原模组请与服务器保持同版本，服务器使用 1.5.0 时无需为本附属模组升级。
2. 退出游戏，把 `simple-autopilot-immersive-aircraft-1.21.1-neoforge-1.2.1.jar` 放进该实例的 `mods` 文件夹。升级时把旧版附属模组移出 mods，不能同时加载两版。
3. 启动游戏。此附属模组只需装在客户端；服务器照常安装原版 Immersive Aircraft。源码包、sources.jar 和其他构建文件不用放进 mods。

支持 Immersive Aircraft 1.5.0／1.5.2 的全部 7 种飞机：

| 游戏中的名称 | 实体 ID |
| --- | --- |
| 飞艇 | `immersive_aircraft:airship` |
| 货运飞船 | `immersive_aircraft:cargo_airship` |
| 战斗飞艇 | `immersive_aircraft:warship` |
| 固定旋翼机（人力飞行器） | `immersive_aircraft:gyrodyne` |
| 双翼机 | `immersive_aircraft:biplane` |
| 四轴飞行器 | `immersive_aircraft:quadrocopter` |
| 竹制浮筒飞机 | `immersive_aircraft:bamboo_hopper` |

## 使用：两个独立开关

- **V：自动前进开／关**，不改变自动高度状态。
- **H：自动高度开／关**，不改变自动前进状态。可以只开高度，前进由你手动控制。
- **Page Up / Page Down** 调整目标高度，每次 2 格，范围 4–64 格，默认 10 格，重启重置。
- S、升降等其他按键不取消、不暂停、也不覆盖已开启的自动轴。V 开启时持续自动前进；H 开启时持续自动控制高度。要手动控制对应轴，先用 V 或 H 关闭它。转向仍由你控制。
- 打开聊天、背包或切出窗口不会使本模组主动停止自动输入。聊天输入中的 V/H 不触发开关。单人游戏的 Esc 菜单、失焦自动暂停等若使整个世界停止 tick，物理运动仍会随游戏暂停；本模组不改变 Minecraft 自身的暂停规则。
- 下机、死亡、离开驾驶位、换载具、换世界或断线仍会重置两个开关，避免在其他场景沿用。
- 在“选项 → 控制 → 按键绑定 → 简单自动驾驶沉浸式飞机”可改键。

只开自动高度不会自动增加油门或启动人力旋翼机，必要时请先手动启动／起飞，或同时开启自动前进。

**双翼机和竹制浮筒飞机使用单独的固定翼控制：** V 持续增加并维持原模组油门，H 根据离地高度、垂直速度和空速调节机头俯仰。只开 V 时俯仰仍可手动操作，只开 H 时油门仍可手动操作。关闭 V 后恢复手动油门；原模组会保留已有油门档位，需要用原模组的减速／减油门键调整，关闭开关不等于关闭发动机。请先在开阔跑道或水面手动起飞，再测试 H；本版未验证自动起飞／着陆。四轴飞行器继续按原模组方式跟随玩家视线方向。

## 定高和障碍预判如何工作

地面参考采用机身下方与前方的碰撞表面，水面、树冠、建筑顶面也会成为高度参考。提前读取前方地形，遇到抬升地形会先爬升；飞艇和旋翼机减少前进输入，固定翼保持油门并抬头；飞过落差后缓慢下降，避免直接向下俯冲。

额外按机身实际碰撞体积连续扫过前方路径，检测墙、窄柱、悬空方块等，避免只靠间隔向下探测而漏过障碍。扫描距离随速度增加，最长 **48 格**；较远障碍触发爬升，飞艇和旋翼机停止额外前进推力，固定翼维持油门避免骤然失速。过近障碍会暂停自动高度修正并提示接管，保留开关；固定翼仍保留 V 控制的油门，无法悬停。移动时主要检查实际速度方向，接近静止时检查机头方向。方向仍由你操纵，不自动绕路。

无法确认地面、区块未加载、256 格向下范围内无表面、头顶空间不足、目标超过世界高度限制或动力不足时，会暂停自动高度修正并提示接管，保留开关；飞艇和旋翼机停止额外前进推力，固定翼保留 V 的油门指令，条件恢复后自动继续。接管时先用 V/H 关闭对应开关。只读取已加载区块，不请求生成远处区块。

**这是辅助驾驶，不保证不撞。** 移除前进输入不等于刹车，关闭后还有惯性。急转弯、高速、陡壁、洞穴、移动障碍、其他实体和复杂建筑需手动驾驶；体积扫描会保守对待斜向路径。它不改坐标、速度、燃料、饥饿或损伤机制，无法突破原机型性能。人力固定旋翼机仍需原本的转子启动过程。建议先在创造测试世界试用。

## 版本与验证范围

本版编译基线降低为 **NeoForge 21.1.1 + Immersive Aircraft 1.5.0+1.21.1**。Minecraft 仍严格限定 1.21.1，不需要升级服务器。

元数据允许 NeoForge `[21.1.1,21.2)` 与 Immersive Aircraft `[1.5.0,1.6)`。在最早发布的 NeoForge 21.1.1 上编译通过，另核对 21.1.243 中本附属模组使用的事件／按键 API；原模组 1.5.0 与 1.5.2 的驾驶注入位置和检查过的机型控制方法一致。范围内其他版本按同一系列兼容性放行，没有逐版实机验证，不能解释为任何版本都能用。

Minecraft 1.20.1、1.21、1.21.11、NeoForge 21.2、Fabric 和旧 Forge 不在本包范围内。1.5.0 以前的原模组、未来 1.6.x 可能有内部接口变化，目前不放行。

48 项自动测试通过，实机飞行和多人游戏仍待测试。验证范围与测试步骤见 [TESTING.md](TESTING.md)。

## 自己编译

需要 64 位 **JDK 21**。首次构建需要联网下载 Gradle、Minecraft 和 NeoForge 依赖。

Windows PowerShell：

```powershell
$env:JAVA_HOME = '你的 JDK 21 安装目录'
.\gradlew.bat build
```

macOS / Linux：

```sh
sh ./gradlew build
```

成品在 `build/libs/`；测试结果在 `build/reports/tests/test/index.html`。开发运行可用 `gradlew.bat runClient`。所有依赖和构建版本已固定，不需要安装额外 Gradle。

## 实现与授权

客户端在原模组 `VehicleEntity.tickPilot()` 的驾驶输入位置，通过 `ModifyArgs` 按机型调整 `setInputs(x, y, z)`：飞艇／旋翼机的 y 是升降、z 是前进；固定翼的 y 是油门、z 是俯仰。保留 x（转向／四轴横移）。高度控制参考实际垂直速度做阻尼，固定翼额外参考空速、机头角度和稳定性升级，经过原模组的输入插值与物理系统。不会修改全局 W 键、原模组 JAR、服务器文件或存档，不新增网络协议。

源码按 **GPL-3.0-only** 提供，完整许可证在 `LICENSE`。原模组归其原作者所有，需另外下载。Gradle Wrapper 来自 [官方 NeoForge 1.21.1 MDK](https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle)，遵循 Apache-2.0；构建使用其 ModDevGradle 2.0.148 配置。

接口依据：[官方 Immersive Aircraft 源码](https://github.com/Luke100000/ImmersiveAircraft/tree/6f900c6a6aad2e251c2902b11d79eb42fdd77324)、[NeoForge 1.21.1 按键文档](https://docs.neoforged.net/docs/1.21.1/misc/keymappings/)。
