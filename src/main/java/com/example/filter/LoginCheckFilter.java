//开发时间 : 2022/10/26 16:05

package com.example.filter;

import com.alibaba.fastjson.JSON;
import com.example.common.BaseContext;
import com.example.entity.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否以登录
 * 使用过滤器还需要在配置类上加@ServletComponentScan
 */
@WebFilter(urlPatterns = "/*",filterName = "LoginCheckFilter") //表示过滤所以路径
@Slf4j
public class LoginCheckFilter implements Filter {

    //路径匹配器,支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String requestURI = request.getRequestURI();
        //设置哪些资源路径不过滤
        String[] urIs = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/user/sendMsg/**",//移动端发送短信
                "/user/login",//移动端登录
        };
        //判断本次请求是否需要处理
        boolean check = check(urIs, requestURI);
        if (check){
            //log.info("本次请求{}不需要处理",requestURI);
            //如果为true,那么表示这些资源不需要拦截
            filterChain.doFilter(request,response);
            return;
        }
        //判断管理端登录状态,如果已经登录就直接放行
        if (request.getSession().getAttribute("employee") != null){
            //log.info("用户以登录,用户id为:{}",request.getSession().getAttribute("employee"));
            Long id = (Long) request.getSession().getAttribute("employee");
            //把id存到线程中,当登录成功后就会把id存到线程里面,当发生修改和新增这些操作需要id的时候,就能直接调用线程的方法,然后在MyMetaObjectHandler.java中自动填充
            //因为在这里面填充了,每次加载新页面,就会把id存到线程中,这样controller需要id的就可以省略了
            BaseContext.setCurrentId(id);
            filterChain.doFilter(request,response);
            return;
        }
//        request.getSession().setAttribute("id",1587420314948620289L);
        //判断客户端登录状态,如果已经登录就直接放行
        if (request.getSession().getAttribute("id") != null){
            Long userid = (Long) request.getSession().getAttribute("id");
            BaseContext.setCurrentId(userid);
            filterChain.doFilter(request,response);
            return;
        }

        //如果未登录则返回未登录结果,通过输出流方式向客户端响应数据
        //因为每个页面,比如backend下的index.html中引入了:<script src="js/request.js"></script>
        //这里面写了前端的拦截器,只要返回的结果是:if (res.data.code === 0 && res.data.msg === 'NOTLOGIN') {// 返回登录页面
        //就会自动跳转到登录页面,所以只需要返回下面这些信息就够了
        response.getWriter().write(JSON.toJSONString(Result.error("NOTLOGIN")));
//        log.info("拦截到了请求:{}",request.getRequestURI());
    }

    /**
     * 路径匹配,检查本次请求是否需要放行
     * @param uris
     * @param requestURI
     * @return
     */
    public boolean check(String[] uris , String requestURI){
        for (String s : uris) {
            boolean match = PATH_MATCHER.match(s, requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }
}
