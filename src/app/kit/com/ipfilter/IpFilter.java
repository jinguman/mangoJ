/**
 * ip-filter
 * https://github.com/madvirus/ip-filter
 * Apache License 2.0
 */
package app.kit.com.ipfilter;

public interface IpFilter {
    boolean accept(String ip);
}