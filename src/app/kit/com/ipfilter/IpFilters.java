/**
 * ip-filter
 * https://github.com/madvirus/ip-filter
 * Apache License 2.0
 */
package app.kit.com.ipfilter;

public class IpFilters {
    public static IpFilter create(Config config) {
        return new ConfigIpFilter(config);
    }
}