/*
 * Copyright 2019 Jeremy Jamet / Kunzisoft.
 *
 * This file is part of KeePass DX.
 *
 *  KeePass DX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  KeePass DX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePass DX.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.kunzisoft.keepass.crypto.keyDerivation

import android.content.res.Resources
import com.kunzisoft.keepass.R
import com.kunzisoft.keepass.crypto.CryptoUtil
import com.kunzisoft.keepass.crypto.finalkey.FinalKeyFactory
import com.kunzisoft.keepass.utils.Types
import java.io.IOException
import java.security.SecureRandom
import java.util.*

class AesKdf internal constructor() : KdfEngine() {

    override val defaultParameters: KdfParameters
        get() {
            val p = KdfParameters(uuid)

            p.setParamUUID()
            p.setUInt32(ParamRounds, DEFAULT_ROUNDS.toLong())

            return p
        }

    override val defaultKeyRounds: Long
        get() = DEFAULT_ROUNDS.toLong()

    init {
        uuid = CIPHER_UUID
    }

    override fun getName(resources: Resources): String {
        return resources.getString(R.string.kdf_AES)
    }

    @Throws(IOException::class)
    override fun transform(masterKey: ByteArray, p: KdfParameters): ByteArray {
        var currentMasterKey = masterKey
        val rounds = p.getUInt64(ParamRounds)
        var seed = p.getByteArray(ParamSeed)

        if (currentMasterKey.size != 32) {
            currentMasterKey = CryptoUtil.hashSha256(currentMasterKey)
        }

        if (seed.size != 32) {
            seed = CryptoUtil.hashSha256(seed)
        }

        val key = FinalKeyFactory.createFinalKey()
        return key.transformMasterKey(seed, currentMasterKey, rounds)
    }

    override fun randomize(p: KdfParameters) {
        val random = SecureRandom()

        val seed = ByteArray(32)
        random.nextBytes(seed)

        p.setByteArray(ParamSeed, seed)
    }

    override fun getKeyRounds(p: KdfParameters): Long {
        return p.getUInt64(ParamRounds)
    }

    override fun setKeyRounds(p: KdfParameters, keyRounds: Long) {
        p.setUInt64(ParamRounds, keyRounds)
    }

    companion object {

        private const val DEFAULT_ROUNDS = 6000

        val CIPHER_UUID: UUID = Types.bytestoUUID(
                byteArrayOf(0xC9.toByte(), 0xD9.toByte(), 0xF3.toByte(), 0x9A.toByte(), 0x62.toByte(), 0x8A.toByte(), 0x44.toByte(), 0x60.toByte(), 0xBF.toByte(), 0x74.toByte(), 0x0D.toByte(), 0x08.toByte(), 0xC1.toByte(), 0x8A.toByte(), 0x4F.toByte(), 0xEA.toByte()))

        const val ParamRounds = "R"
        const val ParamSeed = "S"
    }
}
