package com.mobile.passwordmanager

import androidx.compose.ui.graphics.Color
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.simple_icons.SimpleIcons

/**
 * 内置品牌图标库（离线 Simple Icons 字体，117 个常见网站/APP）。
 *
 * - [IconEntry.key] 以 `sim_` 开头，与历史数据兼容；新增图标只需在此追加。
 * - [IconEntry.color] 为品牌官方主色，用于列表/分组的彩色图标与选择器网格。
 * - [IconEntry.category] 用于选择器分类筛选；[IconEntry.keywords] 支持中英文搜索。
 */
object IconCatalog {

    enum class Category(val label: String) {
        ALL("全部"),
        SOCIAL("社交"),
        FINANCE("金融"),
        SHOPPING("购物"),
        MEDIA("影音"),
        DEV("开发"),
        OFFICE("办公"),
        PLATFORM("系统"),
        GAMING("游戏"),
        LIFE("生活")
    }

    data class IconEntry(
        val key: String,
        val label: String,
        val category: Category,
        val color: Color,
        val keywords: String,
        val icon: IIcon
    )

    val all: List<IconEntry> = listOf(
        IconEntry("sim_wechat", "微信", Category.SOCIAL, Color(0xFF07C160), "微信 weixin wechat", SimpleIcons.Icon.sim_wechat),
        IconEntry("sim_tencentqq", "QQ", Category.SOCIAL, Color(0xFF12B7F5), "qq 腾讯QQ", SimpleIcons.Icon.sim_tencentqq),
        IconEntry("sim_sinaweibo", "微博", Category.SOCIAL, Color(0xFFE6162D), "微博 weibo sina", SimpleIcons.Icon.sim_sinaweibo),
        IconEntry("sim_zhihu", "知乎", Category.SOCIAL, Color(0xFF0066FF), "知乎 zhihu", SimpleIcons.Icon.sim_zhihu),
        IconEntry("sim_kuaishou", "快手", Category.SOCIAL, Color(0xFFFF4906), "快手 kuaishou", SimpleIcons.Icon.sim_kuaishou),
        IconEntry("sim_telegram", "Telegram", Category.SOCIAL, Color(0xFF2AABEE), "telegram 电报", SimpleIcons.Icon.sim_telegram),
        IconEntry("sim_whatsapp", "WhatsApp", Category.SOCIAL, Color(0xFF25D366), "whatsapp", SimpleIcons.Icon.sim_whatsapp),
        IconEntry("sim_messenger", "Messenger", Category.SOCIAL, Color(0xFF0064E0), "messenger", SimpleIcons.Icon.sim_messenger),
        IconEntry("sim_line", "LINE", Category.SOCIAL, Color(0xFF06C755), "line 连我", SimpleIcons.Icon.sim_line),
        IconEntry("sim_kakaotalk", "KakaoTalk", Category.SOCIAL, Color(0xFFFEE500), "kakao", SimpleIcons.Icon.sim_kakaotalk),
        IconEntry("sim_discord", "Discord", Category.SOCIAL, Color(0xFF5865F2), "discord", SimpleIcons.Icon.sim_discord),
        IconEntry("sim_reddit", "Reddit", Category.SOCIAL, Color(0xFFFF4500), "reddit", SimpleIcons.Icon.sim_reddit),
        IconEntry("sim_twitter", "X", Category.SOCIAL, Color(0xFF000000), "twitter x 推特", SimpleIcons.Icon.sim_twitter),
        IconEntry("sim_facebook", "Facebook", Category.SOCIAL, Color(0xFF1877F2), "facebook 脸书", SimpleIcons.Icon.sim_facebook),
        IconEntry("sim_instagram", "Instagram", Category.SOCIAL, Color(0xFFE4405F), "instagram ig", SimpleIcons.Icon.sim_instagram),
        IconEntry("sim_linkedin", "LinkedIn", Category.SOCIAL, Color(0xFF0A66C2), "linkedin", SimpleIcons.Icon.sim_linkedin),
        IconEntry("sim_snapchat", "Snapchat", Category.SOCIAL, Color(0xFFFFFC00), "snapchat", SimpleIcons.Icon.sim_snapchat),
        IconEntry("sim_pinterest", "Pinterest", Category.SOCIAL, Color(0xFFBD081C), "pinterest", SimpleIcons.Icon.sim_pinterest),
        IconEntry("sim_skype", "Skype", Category.SOCIAL, Color(0xFF00AFF0), "skype", SimpleIcons.Icon.sim_skype),
        IconEntry("sim_signal", "Signal", Category.SOCIAL, Color(0xFF3A76F0), "signal", SimpleIcons.Icon.sim_signal),
        IconEntry("sim_vk", "VK", Category.SOCIAL, Color(0xFF0077FF), "vk vkontakte", SimpleIcons.Icon.sim_vk),
        IconEntry("sim_naver", "Naver", Category.SOCIAL, Color(0xFF03C75A), "naver", SimpleIcons.Icon.sim_naver),
        IconEntry("sim_alipay", "支付宝", Category.FINANCE, Color(0xFF1677FF), "支付宝 alipay 支付", SimpleIcons.Icon.sim_alipay),
        IconEntry("sim_applepay", "Apple Pay", Category.FINANCE, Color(0xFF000000), "applepay", SimpleIcons.Icon.sim_applepay),
        IconEntry("sim_googlepay", "Google Pay", Category.FINANCE, Color(0xFF4285F4), "googlepay gpay", SimpleIcons.Icon.sim_googlepay),
        IconEntry("sim_paypal", "PayPal", Category.FINANCE, Color(0xFF003087), "paypal 贝宝", SimpleIcons.Icon.sim_paypal),
        IconEntry("sim_visa", "Visa", Category.FINANCE, Color(0xFF1A1F71), "visa 银行卡", SimpleIcons.Icon.sim_visa),
        IconEntry("sim_mastercard", "Mastercard", Category.FINANCE, Color(0xFFEB001B), "mastercard 银行卡", SimpleIcons.Icon.sim_mastercard),
        IconEntry("sim_americanexpress", "Amex", Category.FINANCE, Color(0xFF2E77BC), "amex 运通", SimpleIcons.Icon.sim_americanexpress),
        IconEntry("sim_binance", "Binance", Category.FINANCE, Color(0xFFF0B90B), "binance 币安", SimpleIcons.Icon.sim_binance),
        IconEntry("sim_coinbase", "Coinbase", Category.FINANCE, Color(0xFF0052FF), "coinbase", SimpleIcons.Icon.sim_coinbase),
        IconEntry("sim_stripe", "Stripe", Category.FINANCE, Color(0xFF635BFF), "stripe", SimpleIcons.Icon.sim_stripe),
        IconEntry("sim_wise", "Wise", Category.FINANCE, Color(0xFF9FE870), "wise transferwise", SimpleIcons.Icon.sim_wise),
        IconEntry("sim_revolut", "Revolut", Category.FINANCE, Color(0xFF6966FF), "revolut", SimpleIcons.Icon.sim_revolut),
        IconEntry("sim_protonmail", "Proton Mail", Category.FINANCE, Color(0xFF6D4AFF), "protonmail 邮箱", SimpleIcons.Icon.sim_protonmail),
        IconEntry("sim_taobao", "淘宝", Category.SHOPPING, Color(0xFFFF5000), "淘宝 taobao", SimpleIcons.Icon.sim_taobao),
        IconEntry("sim_alibabadotcom", "阿里巴巴", Category.SHOPPING, Color(0xFFFF6A00), "阿里巴巴 alibaba", SimpleIcons.Icon.sim_alibabadotcom),
        IconEntry("sim_amazon", "Amazon", Category.SHOPPING, Color(0xFFFF9900), "amazon 亚马逊", SimpleIcons.Icon.sim_amazon),
        IconEntry("sim_ebay", "eBay", Category.SHOPPING, Color(0xFFE53238), "ebay", SimpleIcons.Icon.sim_ebay),
        IconEntry("sim_shopify", "Shopify", Category.SHOPPING, Color(0xFF7AB55C), "shopify", SimpleIcons.Icon.sim_shopify),
        IconEntry("sim_etsy", "Etsy", Category.SHOPPING, Color(0xFFF1641E), "etsy", SimpleIcons.Icon.sim_etsy),
        IconEntry("sim_bilibili", "哔哩哔哩", Category.MEDIA, Color(0xFFFB7299), "b站 bilibili 哔哩哔哩", SimpleIcons.Icon.sim_bilibili),
        IconEntry("sim_tiktok", "TikTok", Category.MEDIA, Color(0xFF000000), "tiktok 抖音", SimpleIcons.Icon.sim_tiktok),
        IconEntry("sim_netflix", "Netflix", Category.MEDIA, Color(0xFFE50914), "netflix 网飞", SimpleIcons.Icon.sim_netflix),
        IconEntry("sim_youtube", "YouTube", Category.MEDIA, Color(0xFFFF0000), "youtube 油管", SimpleIcons.Icon.sim_youtube),
        IconEntry("sim_spotify", "Spotify", Category.MEDIA, Color(0xFF1DB954), "spotify", SimpleIcons.Icon.sim_spotify),
        IconEntry("sim_twitch", "Twitch", Category.MEDIA, Color(0xFF9146FF), "twitch", SimpleIcons.Icon.sim_twitch),
        IconEntry("sim_hbo", "HBO", Category.MEDIA, Color(0xFF7B2BF9), "hbo", SimpleIcons.Icon.sim_hbo),
        IconEntry("sim_applemusic", "Apple Music", Category.MEDIA, Color(0xFFFA243C), "applemusic", SimpleIcons.Icon.sim_applemusic),
        IconEntry("sim_soundcloud", "SoundCloud", Category.MEDIA, Color(0xFFFF5500), "soundcloud", SimpleIcons.Icon.sim_soundcloud),
        IconEntry("sim_github", "GitHub", Category.DEV, Color(0xFF181717), "github 代码", SimpleIcons.Icon.sim_github),
        IconEntry("sim_gitlab", "GitLab", Category.DEV, Color(0xFFFC6D26), "gitlab", SimpleIcons.Icon.sim_gitlab),
        IconEntry("sim_bitbucket", "Bitbucket", Category.DEV, Color(0xFF0052CC), "bitbucket", SimpleIcons.Icon.sim_bitbucket),
        IconEntry("sim_stackoverflow", "Stack Overflow", Category.DEV, Color(0xFFF48024), "stackoverflow 编程", SimpleIcons.Icon.sim_stackoverflow),
        IconEntry("sim_docker", "Docker", Category.DEV, Color(0xFF2496ED), "docker", SimpleIcons.Icon.sim_docker),
        IconEntry("sim_kubernetes", "Kubernetes", Category.DEV, Color(0xFF326CE5), "k8s kubernetes", SimpleIcons.Icon.sim_kubernetes),
        IconEntry("sim_figma", "Figma", Category.DEV, Color(0xFFF24E1E), "figma 设计", SimpleIcons.Icon.sim_figma),
        IconEntry("sim_javascript", "JavaScript", Category.DEV, Color(0xFFF7DF1E), "js javascript", SimpleIcons.Icon.sim_javascript),
        IconEntry("sim_typescript", "TypeScript", Category.DEV, Color(0xFF3178C6), "ts typescript", SimpleIcons.Icon.sim_typescript),
        IconEntry("sim_react", "React", Category.DEV, Color(0xFF61DAFB), "react", SimpleIcons.Icon.sim_react),
        IconEntry("sim_vuedotjs", "Vue", Category.DEV, Color(0xFF42B883), "vue", SimpleIcons.Icon.sim_vuedotjs),
        IconEntry("sim_kotlin", "Kotlin", Category.DEV, Color(0xFFA97BFF), "kotlin", SimpleIcons.Icon.sim_kotlin),
        IconEntry("sim_rust", "Rust", Category.DEV, Color(0xFFDEA584), "rust", SimpleIcons.Icon.sim_rust),
        IconEntry("sim_linux", "Linux", Category.DEV, Color(0xFFFCC624), "linux", SimpleIcons.Icon.sim_linux),
        IconEntry("sim_npm", "npm", Category.DEV, Color(0xFFCB3837), "npm", SimpleIcons.Icon.sim_npm),
        IconEntry("sim_nodedotjs", "Node.js", Category.DEV, Color(0xFF339933), "node nodejs", SimpleIcons.Icon.sim_nodedotjs),
        IconEntry("sim_python", "Python", Category.DEV, Color(0xFF3776AB), "python", SimpleIcons.Icon.sim_python),
        IconEntry("sim_cloudflare", "Cloudflare", Category.DEV, Color(0xFFF38020), "cloudflare", SimpleIcons.Icon.sim_cloudflare),
        IconEntry("sim_vercel", "Vercel", Category.DEV, Color(0xFF000000), "vercel", SimpleIcons.Icon.sim_vercel),
        IconEntry("sim_netlify", "Netlify", Category.DEV, Color(0xFF00C7B7), "netlify", SimpleIcons.Icon.sim_netlify),
        IconEntry("sim_firebase", "Firebase", Category.DEV, Color(0xFFFFCA28), "firebase", SimpleIcons.Icon.sim_firebase),
        IconEntry("sim_amazonaws", "AWS", Category.DEV, Color(0xFFFF9900), "aws amazonwebservices", SimpleIcons.Icon.sim_amazonaws),
        IconEntry("sim_microsoftazure", "Azure", Category.DEV, Color(0xFF0078D4), "azure", SimpleIcons.Icon.sim_microsoftazure),
        IconEntry("sim_googlecloud", "Google Cloud", Category.DEV, Color(0xFF4285F4), "gcp googlecloud", SimpleIcons.Icon.sim_googlecloud),
        IconEntry("sim_openai", "OpenAI", Category.DEV, Color(0xFF10A37F), "openai chatgpt", SimpleIcons.Icon.sim_openai),
        IconEntry("sim_notion", "Notion", Category.OFFICE, Color(0xFF000000), "notion 笔记", SimpleIcons.Icon.sim_notion),
        IconEntry("sim_trello", "Trello", Category.OFFICE, Color(0xFF0052CC), "trello", SimpleIcons.Icon.sim_trello),
        IconEntry("sim_jira", "Jira", Category.OFFICE, Color(0xFF0052CC), "jira", SimpleIcons.Icon.sim_jira),
        IconEntry("sim_slack", "Slack", Category.OFFICE, Color(0xFF4A154B), "slack", SimpleIcons.Icon.sim_slack),
        IconEntry("sim_zoom", "Zoom", Category.OFFICE, Color(0xFF2D8CFF), "zoom 会议", SimpleIcons.Icon.sim_zoom),
        IconEntry("sim_dropbox", "Dropbox", Category.OFFICE, Color(0xFF0061FF), "dropbox 云盘", SimpleIcons.Icon.sim_dropbox),
        IconEntry("sim_googledrive", "Google Drive", Category.OFFICE, Color(0xFF0F9D58), "googledrive 云盘", SimpleIcons.Icon.sim_googledrive),
        IconEntry("sim_icloud", "iCloud", Category.OFFICE, Color(0xFF3693F3), "icloud 云盘", SimpleIcons.Icon.sim_icloud),
        IconEntry("sim_gmail", "Gmail", Category.OFFICE, Color(0xFFEA4335), "gmail 邮箱", SimpleIcons.Icon.sim_gmail),
        IconEntry("sim_microsoftoutlook", "Outlook", Category.OFFICE, Color(0xFF0078D4), "outlook 邮箱", SimpleIcons.Icon.sim_microsoftoutlook),
        IconEntry("sim_evernote", "Evernote", Category.OFFICE, Color(0xFF00A82D), "evernote 笔记", SimpleIcons.Icon.sim_evernote),
        IconEntry("sim_wordpress", "WordPress", Category.OFFICE, Color(0xFF21759B), "wordpress 博客", SimpleIcons.Icon.sim_wordpress),
        IconEntry("sim_adobe", "Adobe", Category.OFFICE, Color(0xFFFF0000), "adobe", SimpleIcons.Icon.sim_adobe),
        IconEntry("sim_google", "Google", Category.PLATFORM, Color(0xFF4285F4), "google 谷歌", SimpleIcons.Icon.sim_google),
        IconEntry("sim_microsoft", "Microsoft", Category.PLATFORM, Color(0xFF0078D4), "microsoft 微软", SimpleIcons.Icon.sim_microsoft),
        IconEntry("sim_apple", "Apple", Category.PLATFORM, Color(0xFF000000), "apple 苹果", SimpleIcons.Icon.sim_apple),
        IconEntry("sim_windows", "Windows", Category.PLATFORM, Color(0xFF0078D4), "windows", SimpleIcons.Icon.sim_windows),
        IconEntry("sim_android", "Android", Category.PLATFORM, Color(0xFF3DDC84), "android", SimpleIcons.Icon.sim_android),
        IconEntry("sim_huawei", "华为", Category.PLATFORM, Color(0xFFCF0A2C), "华为 huawei", SimpleIcons.Icon.sim_huawei),
        IconEntry("sim_xiaomi", "小米", Category.PLATFORM, Color(0xFFFF6900), "小米 xiaomi", SimpleIcons.Icon.sim_xiaomi),
        IconEntry("sim_samsung", "Samsung", Category.PLATFORM, Color(0xFF1428A0), "samsung 三星", SimpleIcons.Icon.sim_samsung),
        IconEntry("sim_oneplus", "OnePlus", Category.PLATFORM, Color(0xFFEB0028), "oneplus", SimpleIcons.Icon.sim_oneplus),
        IconEntry("sim_microsoftbing", "Bing", Category.PLATFORM, Color(0xFF00809D), "bing 必应", SimpleIcons.Icon.sim_microsoftbing),
        IconEntry("sim_yahoo", "Yahoo", Category.PLATFORM, Color(0xFF6001D2), "yahoo", SimpleIcons.Icon.sim_yahoo),
        IconEntry("sim_duckduckgo", "DuckDuckGo", Category.PLATFORM, Color(0xFFDE5833), "duckduckgo 搜索", SimpleIcons.Icon.sim_duckduckgo),
        IconEntry("sim_wikipedia", "Wikipedia", Category.PLATFORM, Color(0xFF3366CC), "wikipedia 维基", SimpleIcons.Icon.sim_wikipedia),
        IconEntry("sim_googlechrome", "Chrome", Category.PLATFORM, Color(0xFF4285F4), "chrome 浏览器", SimpleIcons.Icon.sim_googlechrome),
        IconEntry("sim_firefox", "Firefox", Category.PLATFORM, Color(0xFFFF7139), "firefox 浏览器", SimpleIcons.Icon.sim_firefox),
        IconEntry("sim_safari", "Safari", Category.PLATFORM, Color(0xFF1E90FF), "safari 浏览器", SimpleIcons.Icon.sim_safari),
        IconEntry("sim_microsoftedge", "Edge", Category.PLATFORM, Color(0xFF0078D4), "edge 浏览器", SimpleIcons.Icon.sim_microsoftedge),
        IconEntry("sim_opera", "Opera", Category.PLATFORM, Color(0xFFFF1B2B), "opera 浏览器", SimpleIcons.Icon.sim_opera),
        IconEntry("sim_steam", "Steam", Category.GAMING, Color(0xFF000000), "steam 游戏", SimpleIcons.Icon.sim_steam),
        IconEntry("sim_epicgames", "Epic Games", Category.GAMING, Color(0xFF0074E4), "epic 游戏", SimpleIcons.Icon.sim_epicgames),
        IconEntry("sim_playstation", "PlayStation", Category.GAMING, Color(0xFF0070D1), "ps playstation", SimpleIcons.Icon.sim_playstation),
        IconEntry("sim_xbox", "Xbox", Category.GAMING, Color(0xFF107C10), "xbox", SimpleIcons.Icon.sim_xbox),
        IconEntry("sim_nintendoswitch", "Switch", Category.GAMING, Color(0xFFE60012), "switch nintendo", SimpleIcons.Icon.sim_nintendoswitch),
        IconEntry("sim_minecraft", "Minecraft", Category.GAMING, Color(0xFF62B24F), "minecraft", SimpleIcons.Icon.sim_minecraft),
        IconEntry("sim_roblox", "Roblox", Category.GAMING, Color(0xFF393B3D), "roblox", SimpleIcons.Icon.sim_roblox),
        IconEntry("sim_uber", "Uber", Category.LIFE, Color(0xFF000000), "uber 打车", SimpleIcons.Icon.sim_uber),
        IconEntry("sim_airbnb", "Airbnb", Category.LIFE, Color(0xFFFF5A5F), "airbnb 住宿", SimpleIcons.Icon.sim_airbnb),
        IconEntry("sim_googlemaps", "Google Maps", Category.LIFE, Color(0xFF4285F4), "googlemaps 地图", SimpleIcons.Icon.sim_googlemaps),
        IconEntry("sim_waze", "Waze", Category.LIFE, Color(0xFF00AFF0), "waze 地图", SimpleIcons.Icon.sim_waze),
    ).sortedBy { it.label }

    private val byKey: Map<String, IconEntry> = all.associateBy { it.key }
    val categories: List<Category> = enumValues<Category>().toList()

    fun find(key: String?): IconEntry? = key?.let { byKey[it] }
    fun iconOrNull(key: String?): IIcon? = find(key)?.icon

    /** 取图标品牌色；无图标或未选中时返回主题主色占位。 */
    fun colorOf(key: String?): Color = find(key)?.color ?: Color(0xFF635BED)

    /** 深色主题下提亮过暗的品牌色（如 GitHub / X 的黑色），保证可见。 */
    fun readableColor(c: Color, darkTheme: Boolean): Color {
        val lum = 0.299f * c.red + 0.587f * c.green + 0.114f * c.blue
        return if (darkTheme && lum < 0.45f) {
            Color(
                c.red + (1f - c.red) * 0.5f,
                c.green + (1f - c.green) * 0.5f,
                c.blue + (1f - c.blue) * 0.5f,
                c.alpha
            )
        } else c
    }

    /** 按分类 + 关键词（不区分大小写，匹配名称/key/关键词）筛选。 */
    fun search(query: String, category: Category = Category.ALL): List<IconEntry> {
        val q = query.trim()
        return all.filter { e ->
            (category == Category.ALL || e.category == category) &&
            (q.isEmpty() || e.label.contains(q, ignoreCase = true) ||
                e.key.contains(q, ignoreCase = true) ||
                e.keywords.contains(q, ignoreCase = true))
        }
    }
}
