<img width="1001" height="657" alt="QQ_1792311487161" src="https://github.com/user-attachments/assets/d93e6ca1-f6e6-46b2-8ab5-3a23aee83415" />![正在上传 QQ_1792311487161.png…]()
# Kursh

适用于 Windows 平台单人模式 Unity 游戏的存档编辑器。

Unity 将大部分保存数据以 JSON 格式存储在`%USERPROFILE%\AppData\LocalLow\<company>\<product>`.
Kursh 会定位该文件夹，解析其中的文件，并允许您直接编辑数值字段。它从不
触及正在运行的游戏——您在游戏关闭时编辑存档，然后重新启动游戏。

##为什么不进行内存编辑

向游戏内存写入数据的效果一直有效，直到游戏在下一次重新计算该数值时为止
帧，或者直到你重启且地址已发生变化。保存编辑则不存在这个问题：该
文件才是事实的来源，更改在重启后依然有效。

##使用方法

1.关闭游戏。
2.运行`Kursh.exe`.
3.将游戏文件夹（即包含GameName_Data`的文件夹）粘贴到这里，或点击“浏览”，然后扫描存档**
4.从下拉菜单中选择一个存档。
5.找到该字段：输入其名称的一部分（, `metal`）或其值的一部分（`
整数`/浮点数布尔值芯片与运算符框（=,><1000~5000≈)
进一步缩小范围。
6.双击**新值**单元格，输入数字，按回车键。
7.点击**保存**。文件旁边会先生成一个`.kursh.bak`备份文件。
8.启动游戏并加载该存档。

## Notes

- Only JSON saves are parsed. Binary or encrypted saves are left alone.
- Values are replaced in place. Indentation, key order and every other field stay exactly as
  they were, so the game still reads the file normally. If something looks off, click
  **Restore** to roll back to the backup.
- Editing a save while the game is running usually gets overwritten when the game writes on
  exit. Close the game first.
- The interface is English or Chinese depending on the system language; the button in the
  title bar switches and remembers your choice.

## Build

Needs JDK 17 and JavaFX 17.

```
gradlew jar copyLibs
jpackage --type app-image --name Kursh ^
  --app-version 1.0.0 ^
  --vendor "RenF Games" ^
  --input build\package-libs ^
  --main-jar Kursh-1.0.0.jar ^
  --main-class yudxx.kursh.KurshApp ^
  --icon k.ico ^
  --dest dist ^
  --java-options "-Xmx1g"
```

The result is `dist\Kursh\`, which contains `Kursh.exe`, a bundled runtime and the app jars.
The folder is self-contained — copy it anywhere and run the exe.

## License

MIT. See [LICENSE](LICENSE).
