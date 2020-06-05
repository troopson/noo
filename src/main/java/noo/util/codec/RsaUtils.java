package noo.util.codec;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;

import io.netty.handler.codec.CodecException;
import noo.exception.BusinessException;

/**
 * <p>
 * 描述：Rsa 公钥加密解密工具类
 * </p>
 *
 * @author <a href="mailto:zhouwenbin@ctsi.com.cn">Wen-pin Chou</a>
 */
public final class RsaUtils {

    private RsaUtils() {
    }

    private static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static final String RSA = "RSA";
    private static final String PUBLIC_KEY = "RSAPublicKey";
    private static final String PRIVATE_KEY = "RSAPrivateKey";

    //获得公钥
    public static String getPublicKey(Map<String, Object> keyMap) {
        //获得map中的公钥对象 转为key对象
        Key key = (Key) keyMap.get(PUBLIC_KEY);
        //编码返回字符串
        return Base64.encodeBase64String(key.getEncoded());
    }

    //获得私钥
    public static String getPrivateKey(Map<String, Object> keyMap) {
        //获得map中的私钥对象 转为key对象
        Key key = (Key) keyMap.get(PRIVATE_KEY);
        //编码返回字符串
        return Base64.encodeBase64String(key.getEncoded());
    }

    //map对象中存放公私钥
    public static Map<String, Object> initKey() {
        try {

            //获得对象 KeyPairGenerator 参数 RSA 2048个字节
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(RSA);
            keyPairGen.initialize(2048);
            //通过对象 KeyPairGenerator 获取对象KeyPair
            KeyPair keyPair = keyPairGen.generateKeyPair();

            //通过对象 KeyPair 获取RSA公私钥对象RSAPublicKey RSAPrivateKey
            Key publicKey = keyPair.getPublic();
            Key privateKey = keyPair.getPrivate();
            //公私钥对象存入map中
            Map<String, Object> keyMap = new HashMap<>();
            keyMap.put(PUBLIC_KEY, publicKey);
            keyMap.put(PRIVATE_KEY, privateKey);
            return keyMap;
        } catch (Exception e) {
            throw new BusinessException(String.format("生成密钥失败:%s", e.getMessage()));
        }
    }

    /**
     * 公钥加密
     *
     * @param srcText 源数据
     * @param pubKey  公钥
     * @return 返回base64编码格式密文串
     * @throws CodecException 如果加密失败抛出该异常
     */
    public static String encryptByPublicKey(String srcText, String pubKey) {
        try {
            //换取密钥
            Key key = getPublicKeyByString(pubKey);
            //得到Cipher对象来实现对源数据的RSA加密
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] plainData = srcText.getBytes(StandardCharsets.UTF_8);
            //执行加密操作
            byte[] cipherData = cipher.doFinal(plainData);
            return Base64.encodeBase64String(cipherData);
        } catch (Exception e) {
            throw new BusinessException(String.format("公钥加密失败:%s", e.getMessage()));
        }
    }

    /**
     * 公钥解密
     *
     * @param srcText 数据源
     * @param pubKey  公钥
     * @return 返回base64编码格式明文串
     * @throws CodecException 如果解密失败抛出该异常
     */
    public static String decryptByPublicKey(String srcText, String pubKey) {
        try {
            //换取密钥
            Key key = getPublicKeyByString(pubKey);
            //得到Cipher对象对已用私钥加密的数据进行RSA解密
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] cipherData = Base64.decodeBase64(srcText);
            //执行解密操作
            byte[] plainData = cipher.doFinal(cipherData);
            return new String(plainData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException(String.format("公钥解密失败:%s", e.getMessage()));
        }
    }

    /* @param srcText 源数据
     * @param priKey  私钥
     * @return 返回base64编码格式密文串
     * @throws CodecException 如果加密失败抛出该异常
     */
    public static String encryptByPrivateKey(String srcText, String priKey) {

        try {
            //换取密钥
            Key key = getPrivateKeyByString(priKey);
            //得到Cipher对象来实现对源数据的RSA加密
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] plainData = srcText.getBytes(StandardCharsets.UTF_8);
            //执行加密操作
            byte[] cipherData = cipher.doFinal(plainData);
            return Base64.encodeBase64String(cipherData);
        } catch (Exception e) {
            throw new BusinessException(String.format("私钥加密失败:%s", e.getMessage()));
        }
    }

    /**
     * 私钥解密
     *
     * @param srcText 数据源
     * @param priKey  私钥
     * @return 返回base64编码格式明文串
     * @throws CodecException 如果解密失败抛出该异常
     */
    public static String decryptByPrivateKey(String srcText, String priKey) {
        try {
            //换取密钥
            Key key = getPrivateKeyByString(priKey);
            //得到Cipher对象对已用公钥加密的数据进行RSA解密
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] cipherData = Base64.decodeBase64(srcText);
            //执行解密操作
            byte[] plainData = cipher.doFinal(cipherData);
            return new String(plainData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException(String.format("私钥解密失败:%s", e.getMessage()));
        }
    }

    // 换取公钥密钥
    private static Key getPublicKeyByString(String pubKey) {
        byte[] keyBytes = Base64.decodeBase64(pubKey);
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance(RSA);
            return keyFactory.generatePublic(x509EncodedKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 换取私钥密钥
    private static Key getPrivateKeyByString(String priKey) {
        byte[] keyBytes = Base64.decodeBase64(priKey);
        PKCS8EncodedKeySpec x509EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance(RSA);
            return keyFactory.generatePrivate(x509EncodedKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

}
