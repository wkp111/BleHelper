package com.wkp.blehelperdemo;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by user on 2017/6/8.
 */

public class AESUtils {
    public static Random sRandom = new Random();
    public static byte[] cKeyA = {(byte) 0xB6, (byte) 0xF2, (byte) 0xd4, (byte) 0x5a, (byte) 0xaf, (byte) 0xb2, (byte) 0x72, (byte) 0xDC};
    public final static byte[] cPassWord = {(byte) 0x2E, (byte) 0xCD, (byte) 0xB4, (byte) 0xAC, (byte) 0xA6, (byte) 0x32, (byte) 0xEE,
            (byte) 0x9F, (byte) 0x86, (byte) 0x2E, (byte) 0xEE, (byte) 0x9F, (byte) 0xB6, (byte) 0xF2, (byte) 0x68, (byte) 0xDC};

    public static void main(String[] args) throws Exception {
        byte[] mybyte = {(byte) 0xB6, (byte) 0xF2, (byte) 0xd4, 0x5a, (byte) 0xaf, (byte) 0xb2, 0x72, (byte) 0xDC, (byte) 0xA8, (byte)
                0xBD, 0x31, (byte) 0x94, (byte) 0xAC, 0x3E, (byte) 0xD1, 0x5C};
        byte[] cPassWord = {(byte) 0x2E, (byte) 0xCD, (byte) 0xB4, (byte) 0xAC, (byte) 0xA6, (byte) 0x32, (byte) 0xEE, (byte) 0x9F,
                (byte) 0x86, (byte) 0x2E, (byte) 0xEE, (byte) 0x9F, (byte) 0xB6, (byte) 0xF2, (byte) 0x68, (byte) 0xDC};

        String card_num = "89686524";
        byte[] card = toByte(card_num);
        byte[] encrypt = encrypt(cPassWord, mybyte);

        System.out.println("加密前：" + byteToHex(card));

        System.out.println("加密后：" + byteToHex(encrypt));

        System.out.println("解密后：" + byteToHex(decrypt(encrypt, mybyte)));

    }

    /*
     * 算法/模式/填充 16字节加密后数据长度 不满16字节加密后长度
     * AES/CBC/NoPadding 16 不支持
     * AES/CBC/PKCS5Padding 32 16
     * AES/CBC/ISO10126Padding 32 16
     * AES/CFB/NoPadding 16 原始数据长度
     * AES/CFB/PKCS5Padding 32 16
     * AES/CFB/ISO10126Padding 32 16
     * AES/ECB/NoPadding 16 不支持
     * AES/ECB/PKCS5Padding 32 16
     * AES/ECB/ISO10126Padding 32 16
     * AES/OFB/NoPadding 16 原始数据长度
     * AES/OFB/PKCS5Padding 32 16
     * AES/OFB/ISO10126Padding 32 16
     * AES/PCBC/NoPadding 16 不支持
     * AES/PCBC/PKCS5Padding 32 16
     * AES/PCBC/ISO10126Padding 32 16
     *
     * 注：
     * 1、JCE中AES支持五中模式：CBC，CFB，ECB，OFB，PCBC；支持三种填充：NoPadding，PKCS5Padding，ISO10126Padding。 不带模式和填充来获取AES算法的时候，其默认使用ECB/PKCS5Padding。
     * 2、Java支持的密钥长度：keysize must be equal to 128, 192 or 256
     * 3、Java默认限制使用大于128的密钥加密（解密不受限制），报错信息：java.security.InvalidKeyException: Illegal key size or default parameters
     * 4、下载并安装JCE Policy文件即可突破128密钥长度的限制：覆盖jre\lib\security目录下local_policy.jar、US_export_policy.jar文件即可
     * 5、除ECB外，需提供初始向量（IV），如：Cipher.init(opmode, key, new IvParameterSpec(iv)), 且IV length: must be 16 bytes long
     */

    public static final String ALGORITHM = "AES";
    public static final String TRANSFORMATION = "AES/ECB/NoPadding";

    public static byte[] generate8Bytes() {
        byte[] bytes = new byte[8];
        sRandom.nextBytes(bytes);
        return bytes;
    }
    /**
     * 生成后八位随机的key
     *
     * @return
     */
    public static byte[] generateRandomKey(byte[] bytes) {
        byte[] mKeys = bytes;

        byte[] newBytes = new byte[cKeyA.length + mKeys.length];
        //newStr = str1;数组是引用类型
        for (int x = 0; x < cKeyA.length; x++) {
            newBytes[x] = cKeyA[x];
        }
        for (int y = 0; y < mKeys.length; y++) {
            newBytes[cKeyA.length + y] = mKeys[y];
        }
        return newBytes;
    }

    /**
     * 执行加密
     *
     * @param content
     * @param key     长度必须为16、24、32位，即128bit、192bit、256bit
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(byte[] content, byte[] key) throws Exception {
        if (content.length < 16) {
            byte[] bytes = new byte[16];
            for (int i = 0; i < bytes.length - content.length; i++) {
                bytes[i+content.length] = 0x00;
            }

            for (int i = 0; i < content.length; i++) {
                bytes[i] = content[i];
            }

            content = bytes;
        }
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, ALGORITHM));
        byte[] output = cipher.doFinal(content);
        return output;
    }

    /**
     * 执行加密
     *
     * @param content
     * @param password 作为种子，生成对应的密钥
     * @return
     * @throws Exception
     */
    public static byte[] encrypt(byte[] content, String password) throws Exception {
        if (content.length < 16) {
            byte[] bytes = new byte[16];
            for (int i = 0; i < bytes.length - content.length; i++) {
                bytes[i+content.length] = 0x00;
            }

            for (int i = 0; i < content.length; i++) {
                bytes[i] = content[i];
            }

            content = bytes;
        }
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, generateKey(password));
        byte[] output = cipher.doFinal(content);
        return output;
    }

    /**
     * 执行解密
     *
     * @param content
     * @param key     长度必须为16、24、32位，即128bit、192bit、256bit
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] content, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, ALGORITHM));
        byte[] output = cipher.doFinal(content);
        return output;
    }

    /**
     * 执行解密
     *
     * @param content
     * @param password
     * @return
     * @throws Exception
     */
    public static byte[] decrypt(byte[] content, String password) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, generateKey(password));
        byte[] output = cipher.doFinal(content);
        return output;
    }

    /**
     * 生成随机密钥
     *
     * @return
     * @throws Exception
     */
    public static Key generateKey(int keysize) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(keysize, new SecureRandom());
        Key key = keyGenerator.generateKey();
        return key;
    }

    /**
     * 生成随机密钥
     *
     * @return
     * @throws Exception
     */
    public static Key generateKey() throws Exception {
        return generateKey(128);
    }

    /**
     * 生成固定密钥
     * <p>
     * 作为种子，生成对应的密钥
     *
     * @return
     * @throws Exception
     */
    public static Key generateKey(int keysize, byte[] seed) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(keysize, new SecureRandom(seed));
        Key key = keyGenerator.generateKey();
        return key;
    }

    /**
     * 生成固定密钥
     *
     * @param password 作为种子，生成对应的密钥
     * @return
     * @throws Exception
     */
    public static Key generateKey(int keysize, String password) throws Exception {
        return generateKey(keysize, password.getBytes());
    }

    /**
     * 生成固定密钥
     *
     * @param password 作为种子，生成对应的密钥
     * @return
     * @throws Exception
     */
    public static Key generateKey(String password) throws Exception {
        return generateKey(128, password);
    }

    public static String byteToHex(byte[] bytes) {
        String str = "";
        if (bytes == null) {
            return null;
        }
        for (int i = 0; i < bytes.length; i++) {
            String s = "";
            if (bytes[i] < 0) {
                s = Integer.toHexString(bytes[i]).substring(6, 8);
            } else {
                s = Integer.toHexString(bytes[i]);
            }
            if (s.length() == 1) {
                s = "0" + s;
            }
            str += s;
            str += " ";
        }

        return str.toUpperCase();

    }

    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }

    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];

        for(int i = 0; i < len; ++i) {
            try {
                result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return null;
            }
        }

        return result;
    }

    /**
     * 反向字节数组
     * @param hexString
     * @return
     */
    public static byte[] toDestByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];

        for(int i = 0; i < len; ++i) {
            try {
                result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return null;
            }
        }
        byte[] bytes = new byte[result.length];
        for (int i = 0; i < result.length; i++) {
            bytes[i] = result[result.length - 1 - i];
        }
        return bytes;
    }

    /**
     * 拼接字节数组
     * @param first First array
     * @param rest Any remaining arrays
     * @return Concatenated copy of input arrays
     */
    public static byte[] ConcatArrays(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    public static String toHex(byte[] buf) {
        if(buf == null) {
            return "";
        } else {
            StringBuffer result = new StringBuffer(2 * buf.length);

            for(int i = 0; i < buf.length; ++i) {
                appendHex(result, buf[i]);
            }

            return result.toString();
        }
    }

    private static void appendHex(StringBuffer sb, byte b) {
        String HEX = "0123456789ABCDEF";
        sb.append("0123456789ABCDEF".charAt(b >> 4 & 15)).append("0123456789ABCDEF".charAt(b & 15));
    }
}
