# ===== 体积优化：保留必要的反射/序列化入口，其余交给 R8 裁剪 =====

# Iconics 品牌图标库：SimpleIcons 实现了 ITypeface，可能被框架通过接口/反射持有。
# 保留 typeface 类本身；枚举项由 IconCatalog 静态引用，R8 会自动保留用到的项。
-keep class com.mikepenz.iconics.typeface.library.simple_icons.SimpleIcons { *; }

# Iconics 内部通过 generics/反射访问 drawable 属性，保留其公共 API 签名。
-keep class com.mikepenz.iconics.IconicsDrawable { *; }
-keep interface com.mikepenz.iconics.typeface.ITypeface { *; }
-keep interface com.mikepenz.iconics.typeface.IIcon { *; }
