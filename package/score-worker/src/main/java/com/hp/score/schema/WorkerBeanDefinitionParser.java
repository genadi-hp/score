package com.hp.score.schema;

import com.hp.score.worker.execution.reflection.ReflectionAdapterImpl;
import com.hp.score.worker.execution.services.ExecutionServiceImpl;
import com.hp.score.worker.execution.services.SessionDataServiceImpl;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: 1/21/14
 *
 * @author Dima Rassin
 */
@SuppressWarnings("unused")
public class WorkerBeanDefinitionParser extends AbstractBeanDefinitionParser {

	private Map<Class<?>,String> beans = new HashMap<Class<?>,String>(){{
		put(com.hp.score.worker.management.services.WorkerManager.class, "workerManager");
		put(com.hp.score.events.EventBusImpl.class, null);
		put(ExecutionServiceImpl.class, "agent");
		put(com.hp.score.worker.management.services.InBuffer.class, null);
		put(com.hp.score.worker.management.services.OutboundBufferImpl.class, "outBuffer");
		put(com.hp.score.worker.management.services.RetryTemplate.class, null);
		put(com.hp.score.worker.management.services.SimpleExecutionRunnableFactory.class, null);
		put(com.hp.score.worker.management.services.WorkerManagerMBean.class, "com.hp.oo.execution.services.WorkerManagerMBean");
		put(com.hp.score.worker.management.services.WorkerRecoveryManagerImpl.class, null);
		put(ReflectionAdapterImpl.class, null);
        put(SessionDataServiceImpl.class, "sessionDataService");
		put(com.hp.score.worker.management.services.SynchronizationManagerImpl.class, null);
		put(com.hp.score.worker.management.WorkerConfigurationServiceImpl.class, "workerConfiguration");
//		put(org.score.worker.management.WorkerRegistration.class, null);
	}};

	private List<ConfValue> configurationValues = Arrays.asList(
			new ConfValue().NAME("inBufferCapacity").DEFAULT(500),
			new ConfValue().NAME("numberOfExecutionThreads").DEFAULT(20),
			new ConfValue().NAME("maxDeltaBetweenDrains").DEFAULT(100)
	);

	private List<ConfValue> schedulerValues = Arrays.asList(
			new ConfValue().NAME("outBufferInterval").DEFAULT(100L),
			new ConfValue().NAME("keepAliveInterval").DEFAULT(10000L),
			new ConfValue().NAME("configRefreshInterval").DEFAULT(1000L),
			new ConfValue().NAME("statisticsInterval").DEFAULT(1000L)
	);

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		registerWorkerUuid(element.getAttribute("uuid"), element.getAttribute("depends-on"), parserContext);

		registerBeans(parserContext);

		registerConfiguration(DomUtils.getChildElementByTagName(element, "configuration"), parserContext);

		registerSpringIntegration(parserContext);

		registerScheduler(DomUtils.getChildElementByTagName(element, "scheduler"), parserContext);

		return createRootBeanDefinition();
	}

	private AbstractBeanDefinition createRootBeanDefinition(){
		// todo should be returned some reasonable bean. Currently jus an Object is returned
		return BeanDefinitionBuilder.genericBeanDefinition(Object.class).getBeanDefinition();
	}

	private void registerWorkerUuid(String uuid, String dependsOn, ParserContext parserContext) {
		new BeanRegistrator(parserContext)
				.NAME("workerUuid")
				.CLASS(String.class)
				.addConstructorArgValue(uuid)
				.addDependsOn(StringUtils.hasText(dependsOn)? dependsOn.split(","): null)
				.register();
	}

	private void registerBeans(ParserContext parserContext){
		BeanRegistrator beanRegistrator = new BeanRegistrator(parserContext);
		for (Map.Entry<Class<?>,String> entry : beans.entrySet()) {
			beanRegistrator
					.NAME(entry.getValue())
					.CLASS(entry.getKey())
					.register();
		}
	}

	private void registerSpringIntegration(ParserContext parserContext) {
		new XmlBeanDefinitionReader(parserContext.getRegistry())
				.loadBeanDefinitions("META-INF/spring/score/context/scoreIntegrationContext.xml");
	}

	private void registerConfiguration(Element configurationElement, ParserContext parserContext) {
		for (ConfValue configurationValue : configurationValues) {
			configurationValue.register(configurationElement, parserContext);
		}
	}

	private void registerScheduler(Element schedulerElement, ParserContext parserContext){
		for (ConfValue value : schedulerValues) {
			value.register(schedulerElement, parserContext);
		}
		new XmlBeanDefinitionReader(parserContext.getRegistry())
				.loadBeanDefinitions("META-INF/spring/score/context/scoreWorkerSchedulerContext.xml");
	}

	@Override
	protected boolean shouldGenerateId() {
		return true;
	}

}