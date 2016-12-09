package web.interceptor;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import web.action.LoginAction;
import java.util.Map;

public class MyInterceptor implements Interceptor {

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        System.out.println("[INTERCEPTOR] I JUST RAN");
        Map<String, Object> session = invocation.getInvocationContext().getSession();

        System.out.println("THIS IS THE CODE OF THE INVOCATION: " + invocation.invoke());
        System.out.println("THIS IS THE ACTION OF THE INVOCATION: " + invocation.getAction());

        if(session.containsKey("loggedin")) {
            System.out.println("loggedin");
            return invocation.invoke();
        }

        Object action = invocation.getAction();
        if(!(action instanceof LoginAction)) {
            System.out.println("not an instance of login");
            return "login";
        }

        return invocation.invoke();
    }

    @Override
    public void init() {}

    @Override
    public void destroy() {}
}
