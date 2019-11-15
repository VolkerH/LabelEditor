package com.indago.labeleditor.plugin.interfaces.bvv;

import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.plugin.behaviours.ConflictSelectionBehaviours;
import net.imglib2.roi.labeling.LabelingType;

import java.awt.event.MouseEvent;
import java.util.List;

public class BvvConflictSelectionBehaviours<L> extends ConflictSelectionBehaviours<L> {

	private BvvInterface<L> bvvInterface;

	public void init(LabelEditorModel<L> model, LabelEditorController<L> controller, BvvInterface<L> bvvInterface) {
		super.init(model, controller);
		this.bvvInterface = bvvInterface;
	}

	@Override
	protected void handleMouseMove(MouseEvent e) {
		List<LabelingType<L>> allSets = bvvInterface.getAllLabelsAtMousePosition(e.getX(), e.getY(), model);
		if(allSets == null || allSets.size() == 0) {
			//TODO start collect tagging events, pause listeners
			defocusAll();
			//TODO resume model listeners and send collected events
			return;
		}
		LabelingType<L> labelset = allSets.get(0);
//		for (LabelingType<L> labelset : labels) {
		int intIndex;
		try {
			intIndex = labelset.getIndex().getInteger();
		} catch(ArrayIndexOutOfBoundsException exc) {return;}
		if(intIndex == currentSegment) return;
		currentSegment = intIndex;
		new Thread(() -> {
			//TODO start collect tagging events, pause listeners
			defocusAll();
			currentLabels = labelset;
			labelset.forEach(this::focus);
			//TODO resume model listeners and send collected events
		}).start();
//		}
	}
}