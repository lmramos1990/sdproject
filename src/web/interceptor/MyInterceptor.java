package web.interceptor;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import web.action.LoginAction;
import web.action.RegisterAction;

import java.util.Map;

public class MyInterceptor implements Interceptor {

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        System.out.println("[INTERCEPTOR] I JUST RAN");
        Map<String, Object> session = invocation.getInvocationContext().getSession();

        if(session.containsKey("loggedin")) return invocation.invoke();

        Object action=invocation.getAction();
        if(action instanceof RegisterAction) return invocation.invoke();
        if(!(action instanceof LoginAction)) return "loginRedirect";

        return invocation.invoke();
    }

    @Override
    public void init() {}

    @Override
    public void destroy() {}
}
