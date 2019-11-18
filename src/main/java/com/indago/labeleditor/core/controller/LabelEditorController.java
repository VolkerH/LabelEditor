package com.indago.labeleditor.core.controller;

import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorView;

public class LabelEditorController<L> {

	private LabelEditorModel<L> model;
	private LabelEditorView<L> view;
	private LabelEditorInterface<L> interfaceInstance;

	public void init(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorInterface<L> interfaceInstance) {
		this.model = model;
		this.view = view;
		if(interfaceInstance != null) {
			view.listeners().remove(interfaceInstance::onViewChange);
			model.tagging().listeners().remove(interfaceInstance::onTagChange);
		}
		this.interfaceInstance = interfaceInstance;
		view.listeners().add(interfaceInstance::onViewChange);
		model.tagging().listeners().add(interfaceInstance::onTagChange);
	}

	public void addDefaultBehaviours() {
		interfaceInstance.installBehaviours(model, this, view);
	}

	public void triggerLabelingChange() {
		view.updateOnLabelingChange();
	}

	public LabelEditorInterface<L> interfaceInstance() {
		return interfaceInstance;
	}

	public void install(LabelEditorBehaviours behaviour) {
		behaviour.init(model, this, view);
		behaviour.install(interfaceInstance.behaviours(), interfaceInstance.getComponent());
	}
}
