package com.example.demo.crypto;

import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.junit.Test;


public class RsaEncryptUtils {
	
   public static final String CHARSET = "UTF-8";
  
   public static final String RSA_ALGORITHM = "RSA";
   
   /**
    * @Title: getPublicKey
    * @Description: 获取RSA公钥
    * @Author Administrator
    * @DateTime 2018年11月19日 下午7:48:10
    * @param publicKey
    * @return
    * @throws NoSuchAlgorithmException
    * @throws InvalidKeySpecException 
    */
   public static RSAPublicKey getPublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
       //通过X509编码的Key指令获得公钥对象
       KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
       X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKey));
       RSAPublicKey key = (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
       return key;
   }
   
   /**
    * @Title: getPrivateKey
    * @Description: 获取RSA私钥
    * @DateTime 2018年11月19日 下午7:47:03
    * @param privateKey 密钥字符串（经过base64编码）
    * @return
    * @throws NoSuchAlgorithmException
    * @throws InvalidKeySpecException 
    */    
   public static RSAPrivateKey getPrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
       //通过PKCS#8编码的Key指令获得私钥对象
       KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
       PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey));
       RSAPrivateKey key = (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
       return key;
   }
   
   /**
    * @Title: publicEncrypt
    * @Description: 公钥加密
    * @DateTime 2018年11月19日 下午7:49:09
    * @param data
    * @param publicKey
    * @return 
    */    
   public static String publicEncrypt(String data, RSAPublicKey publicKey) {
       try{
           Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
           cipher.init(Cipher.ENCRYPT_MODE, publicKey);
           return Base64.encodeBase64URLSafeString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET), publicKey.getModulus().bitLength()));
       }catch(Exception e){
           throw new RuntimeException("加密字符串[" + data + "]时遇到异常", e);
       }
   }
   
   /**
    * @Title: privateDecrypt
    * @Description: 私钥解密
    * @DateTime 2018年11月19日 下午7:49:36
    * @param data
    * @param privateKey
    * @return 
    */    
   public static String privateDecrypt(String data, RSAPrivateKey privateKey){
       try{
           Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);

           cipher.init(Cipher.DECRYPT_MODE, privateKey);
           return new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decodeBase64(data), privateKey.getModulus().bitLength()), CHARSET);
       }catch(Exception e){
           throw new RuntimeException("解密字符串[" + data + "]时遇到异常", e);
       }
   }
   

   /**
    * @Title: rsaSplitCodec
    * @Description: RSA分割code
    * @DateTime 2018年11月19日 下午7:50:11
    * @param cipher
    * @param opmode
    * @param datas
    * @param keySize
    * @return 
    */   
   private static byte[] rsaSplitCodec(Cipher cipher, int opmode, byte[] datas, int keySize){
       int maxBlock = 0;
       if(opmode == Cipher.DECRYPT_MODE){
           maxBlock = keySize / 8;
       }else{
           maxBlock = keySize / 8 - 11;
       }
       ByteArrayOutputStream out = new ByteArrayOutputStream();
       int offSet = 0;
       byte[] buff;
       int i = 0;
       try{
           while(datas.length > offSet){
               if(datas.length - offSet > maxBlock){
                   buff = cipher.doFinal(datas, offSet, maxBlock);
               }else{
                   buff = cipher.doFinal(datas, offSet, datas.length-offSet);
               }
               out.write(buff, 0, buff.length);
               i++;
               offSet = i * maxBlock;
           }
       }catch(Exception e){
           throw new RuntimeException("加解密阀值为[" + maxBlock + "]的数据时发生异常", e);
       }
       byte[] resultDatas = out.toByteArray();
       IOUtils.closeQuietly(out);
       return resultDatas;
   }

   @Test
   public void test() throws Exception {
       Map<String, String> keyPairMap = RsaKeyUtils.createKeys(512);
       System.out.println("-----公钥----\n" + keyPairMap.get("publicKey"));
       System.out.println("-----私钥----\n" + keyPairMap.get("privateKey"));
       //System.out.println("-----私钥----\n"+keyPairMap.get("modles"));

       String data= "abc122";

       //1.用公钥加密
       String encode = publicEncrypt(data, getPublicKey(keyPairMap.get("publicKey")));
       System.out.println("-----加密结果----\n"+encode);
       
       //1.用私钥解密
       String decodeResult = privateDecrypt(encode,getPrivateKey(keyPairMap.get("privateKey")));
       System.out.println("-----解密结果----\n"+decodeResult);
   }
 
}
