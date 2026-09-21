
<p align="center">
<img src="https://meteorclient.com/icon.png" alt="meteor-client-logo" width="15%"/>
</p>

<h1 align="center">Meteor</h1>
<p align="center">A Minecraft Fabric Utility Mod for anarchy servers.</p>

<div align="center">
    <a href="https://discord.gg/bBGQZvd"><img src="https://img.shields.io/discord/689197705683140636?logo=discord" alt="Discord"/></a>
    <br>
    <img src="https://img.shields.io/github/last-commit/MeteorDevelopment/meteor-client" alt="GitHub last commit"/>
    <img src="https://img.shields.io/github/commit-activity/w/MeteorDevelopment/meteor-client" alt="GitHub commit activity"/>
    <img src="https://img.shields.io/github/contributors/MeteorDevelopment/meteor-client" alt="GitHub contributors"/>
    <br>
    <img src="https://img.shields.io/github/languages/code-size/MeteorDevelopment/meteor-client" alt="GitHub code size in bytes"/>
    <img src="https://img.shields.io/endpoint?url=https://ghloc.vercel.app/api/MeteorDevelopment/meteor-client/badge?filter=.java$&label=lines%20of%20code&color=blue" alt="GitHub lines of code"/>
</div>

## Usage

### Building
- Clone this repository
- Run `./gradlew build`

### Installation
Follow the [guide](https://meteorclient.com/faq/installation) on the wiki.

## Custom build: 中文汉化 + 内置 Baritone 自动寻路

This fork (built for Minecraft 1.21.8) adds two features on top of upstream:

1. **内置 Baritone（自动寻路）**：Baritone 通过 jar-in-jar 直接打包进 meteor-client 球体，无需在 mods 文件夹单独放置 Baritone。进服后 `#goto x y z`、`#follow`、`#mine` 等 Baritone 命令开箱即用，Meteor 的 Excavator / InfinityMiner 等依赖 Baritone 的模块也会自动启用。
2. **中文汉化**：模块名、模块描述、设置项、设置描述在进入游戏后自动显示为简体中文（词库 3354 条），并内置 CJK 字体（文泉驿微米黑 WenQuanWeiMiHei）作为默认界面字体，中文字符可正常渲染。汉化字典与字体方案参考并改编自 [dingzhen-vape/Meteor-I18n-Support-plugin](https://github.com/dingzhen-vape/Meteor-I18n-Support-plugin)（GPL-3.0，与本项目同协议）。

如需回退为英文界面，可自行删除 `src/main/resources/assets/meteor-client/i18n/zh_cn.json` 与 `src/main/java/meteordevelopment/meteorclient/mixin/i18n/` 后重新构建（同时从 `src/main/resources/fabric.mod.json` 的 mixins 列表移除 `meteor-client-i18n.mixins.json`）。

## Contributions
We will review and help with all reasonable pull requests as long as the guidelines below are met.

- The license header must be applied to all java source code files.
- IDE or system-related files should be added to the `.gitignore`, never committed in pull requests.
- In general, check existing code to make sure your code matches relatively close to the code already in the project.
- Favour readability over compactness.
- If you need help, check out the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) for a reference.

## Bugs and Suggestions
Bug reports and suggestions should be made in this repo's [issue tracker](https://github.com/MeteorDevelopment/meteor-client/issues) using the templates provided.  
Please provide as much information as you can to best help us understand your issue and give a better chance of it being resolved.

## Donations
All of our work is completely free and non-profit (donations pay only for hosting costs), therefore we are very grateful for all donations made to support us in running our community.  
Donations can be made via our [website](https://meteorclient.com/donate) and the minimum amount to get donor benefits is €5.  
You will be rewarded with a role on our Discord server and a customisable in-game cape.  
⚠️ _Make sure to create a Meteor account and link your Discord and Minecraft accounts to fully experience your rewards._ ⚠️

## Credits
[Cabaletta](https://github.com/cabaletta) and [WagYourTail](https://github.com/wagyourtail) for [Baritone](https://github.com/cabaletta/baritone)  
The [Fabric Team](https://github.com/FabricMC) for [Fabric](https://github.com/FabricMC/fabric-loader) and [Yarn](https://github.com/FabricMC/yarn)  
[dingzhen-vape](https://github.com/dingzhen-vape) for the Chinese translation dictionary and CJK font approach ([Meteor-I18n-Support-plugin](https://github.com/dingzhen-vape/Meteor-I18n-Support-plugin), GPL-3.0)

## Licensing
This project is licensed under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html). 

If you use **ANY** code from the source:
- You must disclose the source code of your modified work and the source code you took from this project. This means you are not allowed to use code from this project (even partially) in a closed-source and/or obfuscated application.
- You must state clearly and obviously to all end users that you are using code from this project.
- Your application must also be licensed under the same license.

*If you have any other questions, check our [FAQ](https://meteorclient.com/faq) or ask in our [Discord](https://meteorclient.com/discord) server.*
