package com.life4ever.shadowsocks4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Shadowsocks4jApplicationTest {

    protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

}
