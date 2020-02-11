package com.atguigu.gmall.auth;

import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {
    private static final String pubKeyPath = "D:\\dianshang\\gmall-0805\\JwtTest\\rsa.pub";

    private static final String priKeyPath = "D:\\dianshang\\gmall-0805\\JwtTest\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "J@z!P!.I!S#N@!*&B!");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1ODA3MjQyOTl9.IuWvcl1S3fzzCRq3CRQlVWu_WQzFdsfFkXr204y9QKPkrYwGPXDhpfcCM9iJjSBHqk_inFOsMfEhH8vDItMslnsxNNxWbKmag9zhg3GXC5InnCLt5szqb4nrIJZ6awQsz49qeesdAhyvJDk4YnyvFMEOrVpHzVfHQLooomuANJxvReuBMTqehUxZ_6sqr0Wag4bMiE3rtjTslgznL36KhNtDMMWiMup5tMD2muA1GwkKs5PvMzHkvIBI_64rHlMpM-6wqLGuWYgPdexIlYbp1ocvcZAEx5bBuGmezBjH3b9Xk7KqHPoV5DK9Rvl2aFCE72tEEdhghRuMRYzewFRbGw";

        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}
