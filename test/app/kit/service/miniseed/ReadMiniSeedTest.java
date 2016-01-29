package app.kit.service.miniseed;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import app.kit.com.conf.MangoConf;

@RunWith(SpringJUnit4ClassRunner.class) 
@ContextConfiguration(classes={MangoConf.class})
public class ReadMiniSeedTest {

	ReadMiniSeed readMS;
	private static ApplicationContext context;
	
    @Autowired
    public void init(ApplicationContext context) {
        ReadMiniSeedTest.context = context;
    }
	
	@Test
	public void test() {
		
		File file = new File("D:/temp/2016/022/AK/ANM/AK.ANM..BHE.2016.022.00.00.00");
		boolean isDbCheck = false;
		readMS = context.getBean(ReadMiniSeed.class, file, isDbCheck);
		readMS.run();
	}

}
