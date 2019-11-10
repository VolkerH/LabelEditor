package com.indago.labeleditor.plugin.interfaces.bvv;

import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.plugin.behaviours.ConflictSelectionBehaviours;
import net.imglib2.roi.labeling.LabelingType;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.List;

public class BvvConflictSelectionActions<L> extends ConflictSelectionBehaviours<L> {

	private final BvvInterface<L> bvvInterface;

	public BvvConflictSelectionActions(LabelEditorModel<L> model, LabelEditorView<L> renderer, LabelEditorController<L> actionManager, BvvInterface<L> bvvInterface) {
		super(model, renderer, actionManager);
		this.bvvInterface = bvvInterface;
		install( bvvInterface.getBvvHandle().getTriggerbindings(), "labeleditor-bvv" );
		MouseMotionListener mml = new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {}
			@Override
			public void mouseMoved(MouseEvent e) {
				getMouseMoveBehaviour().move(e);
			}
		};
		bvvInterface.getBvvHandle().getViewerPanel().getDisplay().addMouseMotionListener( mml );
	}

	@Override
	protected void handleMouseMove(MouseEvent e) {
		List<LabelingType<L>> allSets = bvvInterface.getAllLabelsAtMousePosition(e, model);
		if(allSets == null || allSets.size() == 0) {
			//TODO pause model listeners
			defocusAll();
			//TODO resume model listeners
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
			//TODO pause model listeners
			defocusAll();
			currentLabels = labelset;
			labelset.forEach(this::focus);
			//TODO resume model listeners
		}).start();
//		}
	}
}
