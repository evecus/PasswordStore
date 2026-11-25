package com.mobile.passwordmanager

import java.security.SecureRandom

object PasswordGenerator {

    private val LOWER = "abcdefghijkmnpqrstuvwxyz".toCharArray() // 去掉易混淆 l,o
    private val UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray() // 去掉 I,O
    private val DIGITS = "23456789".toCharArray()                // 去掉 0,1
    private val SYMBOLS = "!@#\$%&*+-=?".toCharArray()

    /** 生成指定长度、可选字符集的随机密码,保证至少包含每类一个字符(若启用)。 */
    fun generate(
        length: Int = 16,
        useUpper: Boolean = true,
        useDigits: Boolean = true,
        useSymbols: Boolean = true
    ): String {
        val len = length.coerceIn(4, 64)
        val pools = ArrayList<CharArray>()
        pools.add(LOWER)
        if (useUpper) pools.add(UPPER)
        if (useDigits) pools.add(DIGITS)
        if (useSymbols) pools.add(SYMBOLS)

        val rnd = SecureRandom()
        val out = StringBuilder(len)
        // 先各放一个,保证覆盖
        pools.forEach { out.append(it[rnd.nextInt(it.size)]) }
        // 合并池用于剩余位
        val all = pools.flatMap { it.asList() }
        repeat(len - pools.size) {
            out.append(all[rnd.nextInt(all.size)])
        }
        // 洗牌
        val shuffled = out.toString().toCharArray().apply {
            for (i in size - 1 downTo 1) {
                val j = rnd.nextInt(i + 1)
                val tmp = this[i]; this[i] = this[j]; this[j] = tmp
            }
        }
        return String(shuffled)
    }
}
