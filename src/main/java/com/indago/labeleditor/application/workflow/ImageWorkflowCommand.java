package com.indago.labeleditor.application.workflow;

import net.imagej.ops.OpService;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;
import org.scijava.Cancelable;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.module.Module;
import org.scijava.module.ModuleException;
import org.scijava.plugin.Parameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * This command always processes one {@link RandomAccessibleInterval}. The output(s) should be defined by the classes extending this class.
 * The image processing workflow should be called in the {@link #run(RandomAccessibleInterval)} method.
 * If a command is called which needs to be
 *
 * @author Deborah Schmidt
 */
public abstract class ImageWorkflowCommand implements Command, Cancelable {

	@Parameter
	RandomAccessibleInterval input;

	@Parameter
	protected OpService opService;

	@Parameter
	protected Context context;

	@Parameter
	private WorkflowModuleService workflowModuleService;

	@Parameter
	protected CommandService commandService;

	private ImageWorkflow commandWorkflow;

	private boolean testRun = true;

	private Map<ImageWorkflowStep, Module> modules = new HashMap<ImageWorkflowStep, Module>();

	private boolean canceled = false;

	public abstract String getTitle();

	public abstract void run(RandomAccessibleInterval img) throws Exception;

	@Override
	public void run() {
		initWorkflow();
		RandomAccessibleInterval testImg = createTestImage();
		harvestInputsDuringTestRun(testImg);
		doMainRun();
	}

	protected void initWorkflow() {
		commandWorkflow = new SwingImageWorkflow();
		context.inject(commandWorkflow);
		commandWorkflow.initWorkflow(getTitle(), getSteps());
		modules.clear();
	}

	protected abstract List<ImageWorkflowStep> getSteps();

	protected RandomAccessibleInterval createTestImage() {
		long[] min = new long[input.numDimensions()];
		long[] max = new long[input.numDimensions()];
		for (int i = 0; i < min.length; i++) {
			min[i] = 0;
			max[i] = 9;
		}
		Interval interval = new FinalInterval(min, max);
		return opService.copy().rai(Views.interval(input, interval));
	}

	private void harvestInputsDuringTestRun(RandomAccessibleInterval testImg) {
		testRun = true;
		commandWorkflow.startPreviewRun();
		try {
			run(testImg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commandWorkflow.donePreviewRun();
	}

	private void doMainRun() {
		testRun = false;
		commandWorkflow.startMainRun();
		try {
			run(input);
		} catch (Exception e) {
			e.printStackTrace();
		}
		commandWorkflow.doneMainRun();
	}

	protected Module runCommand(ImageWorkflowStep step, Class commandClass, Object... args) throws ModuleException, ExecutionException, InterruptedException {
		if(testRun) commandWorkflow.setTestStep(step);
		else commandWorkflow.setMainStep(step);
		Module module;
		if(testRun) {
			module = createModule(commandClass, args);
			modules.put(step, module);
			commandWorkflow.addCommand(step, module);
			module = workflowModuleService.runSilently(module).get();
		} else {
			module = modules.get(step);
//			module.getOutputs().forEach((name, o) -> module.unresolveOutput(name));
			setModuleInputs(module, args);
			module = workflowModuleService.runWithPostprocessing(module).get();
		}
		return module;
	}

	private Module createModule(Class commandClass, Object... args) throws ModuleException {
		Module module = commandService.getCommand(commandClass).createModule();
		setModuleInputs(module, args);
		return module;
	}

	private void setModuleInputs(Module module, Object[] args) {
		assert(args.length % 2 == 0);

		workflowModuleService.preprocessWithoutHarvesting(module);

		for (int i = 0; i < args.length-1; i+=2) {
			String input = (String) args[i];
			module.setInput(input, args[i+1]);
			module.resolveInput(input);
		}
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public void cancel(String reason) {
		canceled = true;
	}

	@Override
	public String getCancelReason() {
		return null;
	}
}
