/* 
 * 作者：钟勋 (e-mail:zhongxunking@163.com)
 */

/*
 * 修订记录:
 * @author 钟勋 2016-12-31 18:56 创建
 */
package top.bekit.flow.transaction;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ClassUtils;
import top.bekit.flow.annotation.transaction.FlowTx;
import top.bekit.flow.engine.TargetContext;
import top.bekit.flow.transaction.FlowTxExecutor.FlowTxMethodExecutor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * 流程事务解析器
 */
public class FlowTxParser {

    /**
     * 解析流程事务
     *
     * @param flowTx    流程事务
     * @param txManager 事务管理器
     * @return 流程事务执行器
     */
    public static FlowTxExecutor parseFlowTx(Object flowTx, PlatformTransactionManager txManager) {
        FlowTx flowTxAnnotation = flowTx.getClass().getAnnotation(FlowTx.class);
        // 创建流程事务执行器
        FlowTxExecutor flowTxExecutor = new FlowTxExecutor(flowTxAnnotation.flow(), flowTx, txManager);
        for (Method method : flowTx.getClass().getDeclaredMethods()) {
            for (Class clazz : FlowTxExecutor.FLOW_TX_METHOD_ANNOTATIONS) {
                if (method.isAnnotationPresent(clazz)) {
                    // 设置流程事务方法执行器
                    flowTxExecutor.setMethodExecutor(clazz, parseFlowTxMethod(method));
                    break;
                }
            }
        }
        flowTxExecutor.validate();

        return flowTxExecutor;
    }

    // 解析流程事务方法
    private static FlowTxMethodExecutor parseFlowTxMethod(Method method) {
        // 校验方法类型
        if (!Modifier.isPublic(method.getModifiers())) {
            throw new IllegalArgumentException("流程事务方法" + ClassUtils.getQualifiedMethodName(method) + "必须是public类型");
        }
        // 校验入参
        Class[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException("流程事务方法" + ClassUtils.getQualifiedMethodName(method) + "的入参必须是（TargetContext）");
        }
        if (parameterTypes[0] != TargetContext.class) {
            throw new IllegalArgumentException("流程事务方法" + ClassUtils.getQualifiedMethodName(method) + "的入参必须是（TargetContext）");
        }
        // 校验返回参数
        if (method.getReturnType() == void.class) {
            throw new IllegalArgumentException("流程事务方法" + ClassUtils.getQualifiedMethodName(method) + "的返回类型不能是void，需要返回操作后的目标对象");
        }

        return new FlowTxMethodExecutor(method);
    }
}
