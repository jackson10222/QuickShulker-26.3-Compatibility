# Quick Shulker Multi

[[English]](../README.md) | [简体中文]

#### 无需放置 快速打开潜影盒（以及更多），并使用preprocessor进行重构

支持 `1.20.6+` 的 `Minecraft Java版`  
欢迎为此模组添加本地化！

## Minecraft 26.3 兼容适配版本

这是基于 Quick Shulker Multi 制作的 **非官方 Minecraft 26.3 兼容适配版本**。本分支新增的改动仅针对 QuickShulker 与 **Easy Anvils**、**Visual Workbench** 的虚拟菜单兼容性：

- 通过 QuickShulker 快速打开这两个模组替换的方块时，使用 Minecraft 对应的原版铁砧菜单或工作台菜单。
- 在世界中正常交互已放置的方块时，仍由 Easy Anvils 和 Visual Workbench 各自处理。
- 本分支不会修改、打包或重新分发这两个模组，也与它们的作者没有隶属或合作关系。

**原作者与致谢：** 最初的 QuickShulker 模组由 [kyrptonaught](https://github.com/kyrptonaught/quickshulker) 创作。本项目也基于后续移植和维护者 [Grayer0113](https://github.com/Grayer0113/quickshulker)、[Haocen2004](https://github.com/Haocen2004/quickshulker)、[MoRanpcy](https://github.com/MoRanpcy/quickshulker)，以及由 EnderPhantomWing 维护的 [Quick Shulker Multi 上游项目](https://github.com/EnderPhantomWing/quickshulker-multi) 的工作。请同时参阅项目现有的 [LICENSE](../LICENSE) 和第三方声明。

使用多版本构建工具 `preprocessor` 快速构建多个 Minecraft 版本的 mod。

此模组上游仓库 [MoRanpcy/quickshulker](https://github.com/MoRanpcy/quickshulker) 更新自 [kyrptonaught](https://github.com/kyrptonaught)的 [QuickShulker](https://github.com/kyrptonaught/quickshulker.git) 模组 支持高版本同时修复部分bug。

模组国际化和本地化需要您的帮助！如果可以的话那太感谢你了！

### 模组命名规范

 * 将会在下个大版本更新中更新模组命名方式

| build               | naming                                                                                                     |
|---------------------|------------------------------------------------------------------------------------------------------------|
| release             | quickshulker-v{mod_version}-mc{mc_version}-{commit_count}-{commit_hash}-release.jar                        |
| pull_request        | quickshulker-v{mod_version}-mc{mc_version}-{build_branch}-{build_branch_commit_count}-{commit_hash}-pr.jar |
| ci                  | quickshulker-v{mod_version}-mc{mc_version}-{build_branch}-{build_branch_commit_count}-{commit_hash}-ci.jar |
| local               | quickshulker-v{mod_version}-mc{mc_version}-{build_branch}-{build_timestampmillis}-development.jar          |
| maven               | {mc_version}-{mod_version}.jar                                                                             |
| **EXAMPLE BUILDS:** | **EXAMPLE NAMING:**                                                                                        |
| release             | `quickshulker-v3.2.6-mc1.21.11-187-6068667-release.jar`                                                    |
| pull_request        | `quickshulker-v3.2.6-mc1.21.11-dev-187-eba238a-pr.jar`                                                     |
| ci                  | `quickshulker-v3.2.7-mc1.21.11-dev-192-6fb9034-ci.jar`                                                     |
| local               | `quickshulker-v3.2.7-mc1.21.11-dev-1782234626080-development.jar`                                          |
| maven               | `1.21.11-3.2.7.jar`                                                                                        |

本 Minecraft 26.3 兼容版本在本地 Build 时，生成的文件名为 `quickshulker-1.0.0+mc26.3.jar`。


## 下载

单击下面的链接进行下载。  
请为您的 Minecraft 版本选择合适的模组。

* [最新Release (GitHub Release)](https://github.com/EnderPhantomWing/quickshulker-multi/releases/latest)

* [最新快照 (GitHub Actions)](https://github.com/EnderPhantomWing/quickshulker-multi/actions)

### 感谢

- [kyrptonaught/quickshulker](https://github.com/kyrptonaught/quickshulker) 模组作者
- [Grayer0113/quickshulker](https://github.com/Grayer0113/quickshulker/tree/1.20) 支持 MC 1.20.4
- [Haocen2004/quickshulker](https://github.com/Haocen2004/quickshulker) 支持 MC 1.20.6 和 1.21(.1)
- [MoRanpcy/quickshulker](https://github.com/MoRanpcy/quickshulker) 支持 MC 1.21+ 并且修复部分bug
- 以及所有支持开发的人，包括你！

### 许可证与第三方来源

本项目遵循 [MIT 许可证](https://mit-license.org/)  
[kyrptonaught/quickshulker](https://github.com/kyrptonaught/quickshulker) 是提供模组整体功能的初始版本  
[MoRanpcy/quickshulker](https://github.com/MoRanpcy/quickshulker) 移植模组至 1.21+ 版本  
[许可证](../LICENSE) | [第三方声明](THIRD_PARTY_NOTICES.md)