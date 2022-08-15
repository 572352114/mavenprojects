package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*过滤器   检查用户是否完成登录*/
@WebFilter(filterName = "loginCherckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {
    //专门用来路径比较的路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();


    /*初始化方法
     * 1.只在初始化的时候执行一次
     * 2.接收一个FilterConfig类型的参数，该参数是对Filter的一些配置
     * */
    public void init(FilterConfig config) throws ServletException {
    }

    /*销毁时调用*/
    public void destroy() {
    }

    /*过滤方法
     * 1.主要是对request和response进行一些处理,然后交给下一个过滤器或Servlet处理
     * */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //需要把servletRequest强转成HttpServletRequest
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        //需要把servletResponse强转成HttpServletResponse
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        log.info("拦截到请求{}",request.getRequestURI());
        //1.获取本次请求的URI
        String resqusetURI = request.getRequestURI();

        //2.定义不需要处理的请求路径，判断本次请求是否需要处理
        String[] urls = new String[]{
                "/employee/login",//网页端登录
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",//手机端登录
                "/user/login",
                "/user/logout"
        };
        boolean check = check(resqusetURI,urls);

        //3.如果不需要处理，直接放行
        if(check){
            //放行
            filterChain.doFilter(request,response);
            log.info("本次请求{}无需处理",request.getRequestURI());
            //放行之后 后面的代码无需执行
            return;
        }
        /** 后台网页端*/
        //4-1. 判断登录状态，如果已登录，直接放行
        if(request.getSession().getAttribute("employee")!=null){
            //已登录 说明Session中已经通过setAttribute(name,value) 存了employee属性和值
            //所以可以直接调用getAttribute判断该属性的值是否存在  存在即表明已登录 直接放行
            Long empid = (Long) request.getSession().getAttribute("employee");

            //将当前的登录id存入线程变量中去   通过common文件下的BaseContext类中的setCurrentId方法
            BaseContext.setCurrentId(empid);

            filterChain.doFilter(request,response);

            log.info("用户已登录，用户id为{}",empid);
            return;
        }

        /** 手机移动端*/
        //4-2.判断登录状态，如果已登录，直接放行
        if(request.getSession().getAttribute("user")!=null){
            //已登录 说明Session中已经通过setAttribute(name,value) 存了user属性和值
            //所以可以直接调用getAttribute判断该属性的值是否存在  存在即表明已登录 直接放行
            Long userid = (Long) request.getSession().getAttribute("user");
            log.info("用户已登录，用户id为{}",userid);
            //将当前的登录id存入线程变量中去   通过common文件下的BaseContext类中的setCurrentId方法
            BaseContext.setCurrentId(userid);

            filterChain.doFilter(request,response);
            return;
        }

        //5.如果未登录则返回未登录结果,通过输出流的方式向客户端返回结果
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        log.info("用户未登录");
        return;
    }

    /**
     * 路径匹配方法，检查本次请求是否需要放行
     * @param requsetURL 本次请求路径
     * @param urls       可以放行的路径
     * @return
     */
    public boolean check(String requsetURL,String[] urls){
        for (String url : urls) {
            //boolean match = PATH_MATCHER.match(url, requsetURL);
            val match = PATH_MATCHER.match(url, requsetURL);
            if(match==true){
                return true;
            }
        }
        return false;
    }
}
