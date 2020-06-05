package noo.util.codec;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom; 

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import noo.exception.BusinessException;
 

/**
 * <code>AesUtils</code>是一个Aes 加解密工具。
 *
 * <p>
 *
 * <ul>
 * <li>在这个加解密工具来中，使用的加密算法为AES加密算法，模式为CBC，填充方式为PKCS5Padding；可参考{@value
 * #CIPHER_ALGORITHM}。以下Aes加解密使用的块长度为默认128位，所以在使用密钥和偏移量的时候，密钥长度要大于16位；{@linkplain
 * #getRandomString(int) 生成随机串}时，可以选择长度为16、24、32等。
 * <li>加密过程是： 明文串->{@linkplain #encrypt(String, String, String) AES加密}->{@linkplain
 * Base64#encodeBase64String(byte[]) Base64转码}->密文串。
 * <li>解密过程是：密文串->{@linkplain Base64#decodeBase64(String) Base64解码}->{@linkplain
 * #decrypt(String, String, String)
 * AES解密}->明文串。
 * <li>使用示例：
 * <pre>
 *    ...
 *    String key = AesUtils.getRandomString(16);
 *    String iv = AesUtils.getRandomString(16);
 *    String plainText = "AES CBC模式加密";
 *    // 加密
 *    String cipherText = AesUtils.encrypt(plainText, key, iv);
 *    // 解密
 *    String targetText = AesUtils.decrypt(cipherText, key, iv);
 *    ...
 *      </pre>
 * </ul>
 *
 * @author <a href="mailto:zhouwenbin@ctsi.com.cn">Wen-pin Chou</a>
 * @see #getRandomString(int)
 * @see #encrypt(String, String, String)
 * @see #decrypt(String, String, String)
 * @see Base64
 * @see URLEncoder
 * @see URLDecoder
 * @see Cipher
 */
public final class AesUtils {

    // 编码
    private static final String ENCODING = "UTF-8";
    // 算法
    private static final String ALGORITHM = "AES";
    // 默认的加密算法
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    private AesUtils() {
    }

    /**
     * 使用Aes CBC模式加密
     *
     * @param data   明文数据
     * @param key    密钥串
     * @param offset 向量串
     * @return Base64编码格式密文
     * @throws CodecException 如果加密失败，抛出异常
     */
    public static String encrypt(String data, String key, String offset) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            Key skeySpec = toKey(key);
            IvParameterSpec iv = new IvParameterSpec(offset.getBytes()); // 使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(data.getBytes(ENCODING));
            return Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            throw new BusinessException(String.format("加密异常:%s", e.getMessage()));
        }
    }

    /**
     * 使用Aes CBC模式
     *
     * @param data   Base64编码格式密文
     * @param key    密钥串
     * @param offset 向量串
     * @return 加密前的字符串
     * @throws CodecException 如果解密失败，抛出异常
     */
    public static String decrypt(String data, String key, String offset) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            Key skeySpec = toKey(key);
            IvParameterSpec iv = new IvParameterSpec(offset.getBytes()); // 使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] buffer = Base64.decodeBase64(data);
            byte[] encrypted = cipher.doFinal(buffer);
            return new String(encrypted, ENCODING); // 此处使用BASE64做转码。
        } catch (Exception e) {
            throw new BusinessException(String.format("解密异常:%s", e.getMessage()));
        }
    }

    /**
     * 通过本方法可以得到一个一定长度的随机字符串，这个字符串是有英文大小写和数字组成
     *
     * @param length 目标字符串的长度
     * @return {@link String}
     */
    public static String getRandomString(int length) {

        if (length < 1) {
            throw new IllegalStateException();
        }
        StringBuilder ret = new StringBuilder();
        SecureRandom random = new SecureRandom();
        int data;
        for (int i = 0; i < length; i++) {

            int index = random.nextInt(3);
            switch (index) {
                case 0:
                    data = random.nextInt(10);
                    ret.append(data);
                    break;
                case 1:
                    data = random.nextInt(26) + 65;
                    ret.append((char) data);
                    break;
                case 2:
                    data = random.nextInt(26) + 97;
                    ret.append((char) data);
                    break;
                default:
                    // do nothing
                    break;
            }
        }
        return ret.toString();
    }

    // 换取密钥
    private static Key toKey(String key) {
        return new SecretKeySpec(key.getBytes(StandardCharsets.US_ASCII), ALGORITHM);
    }
}
