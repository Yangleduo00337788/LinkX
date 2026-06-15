# 托盘图标说明

请在此目录放置以下图标文件：

- `tray.png` - 16x16 像素的托盘图标
- `tray@2x.png` - 32x32 像素的高清托盘图标

## 图标要求

1. **格式**: PNG (支持透明背景)
2. **尺寸**: 
   - Windows: 16x16 或 32x32
   - macOS: 22x22 (Retina: 44x44)
3. **设计**: 简洁的单色或双色图标，适合小尺寸显示

## 推荐设计

- 使用字母 "L" 或 "X" 作为主元素
- 颜色: #18a058 (绿色) 或 #2080f0 (蓝色)
- 背景: 透明或圆角矩形

## 在线工具

可以使用以下工具创建图标：
- https://iconworkshop.app/
- https://www.figma.com/
- https://www.canva.com/

或者使用简单命令行工具 ImageMagick：
```bash
convert -size 32x32 xc:transparent -fill "#18a058" -draw "roundrectangle 0,0 31,31 6,6" -fill white -font "Arial-Bold" -pointsize 20 -gravity center -annotate +0+0 "L" tray.png
```
