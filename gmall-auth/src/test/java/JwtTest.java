import com.atguigu.core.utils.JwtUtils;
import com.atguigu.core.utils.RsaUtils;

import org.junit.Before;
import org.junit.Test;


import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {
    private static final String pubKeyPath = "D:\\project\\idea\\github\\JWT\\rsa.pub";

    private static final String priKeyPath = "D:\\project\\idea\\github\\JWT\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "J@z!P!.6^J!ia$%#N%$@!-=*&B!");
        System.out.println("ok");
    }

    //
    //@Before
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
        String token = JwtUtils.generateToken(map, privateKey, 10000);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MywidXNlcm5hbWUiOiJRUVEiLCJleHAiOjE1ODQ0NTU5Njd9.FCBS6pqk1zCedw7qbmewfgBhyxm1qSk7IvxatHmrES9DWWG13HUHx_Ox7Dg1kY6EwQ93EOf3vTGdBqb_tXu_dvWeFKULp3agX7NKXuFUWaZEun9l_2hpvqdq_5m3lsUGs0BwrtmUtpb-N9hdaw_6zOFDFXt3Rx1KBrK5ctnoOiPGWKUZ_TaGfsZu5F-kR360z_RqNh_cGwp64UxZR3PU9BbTXiiuZ81ZWDAyobU4ZHudZLKURlm_E11VWfoO88pHUCJV_oEk4crgaeRxJPdVpmNZzXMj00nBUt126FZ6ZQHI7Q41D0n-YzdGIIfwfPFDXEdtLqWgYsIsfBIBRNEeEw";
        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }

    @Test
    public void testTime() {
        Date date = new Date();
        System.out.println(date);
    }
}
