package app.kit.com.ipfilter;

import org.junit.Test;

public class IpFilterTest {

	private static String[] ipPatterns = {
            "1.2.3.4",
            "1.2.3.5",
            "1.2.3.64/26",
            "10.20.*",
            "10.10.10.*"
    };
	
	
	@Test
	public void test() {
		
		Config config = new Config();
        config.setDefaultAllow(false);
        config.setAllowFirst(true);
        for (String ipPattern : ipPatterns)
            config.allow(ipPattern);
		
        IpFilter filter = IpFilters.create(config);
        
        System.out.println(filter.accept("10.10.10.2"));
	}
}
