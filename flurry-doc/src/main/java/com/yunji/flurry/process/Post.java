package com.yunji.flurry.process;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Denim.leihz 2019-08-01 2:57 PM
 */
public interface Post {

    String post(String service, String version,
                String method, String parameter,
                HttpServletRequest req) throws Exception;
}
