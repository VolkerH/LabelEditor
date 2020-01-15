package sc.fiji.labeleditor.plugin.interfaces.bdv;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import net.imglib2.RandomAccessibleInterval;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;
import org.scijava.Disposable;
import org.scijava.command.CommandService;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import sc.fiji.labeleditor.core.DefaultInteractiveLabeling;
import sc.fiji.labeleditor.core.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.DefaultLabelEditorController;
import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.controller.LabelEditorInterface;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.DefaultLabelEditorView;
import sc.fiji.labeleditor.core.view.LabelEditorView;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelEditorBdvPanel extends JPanel implements Disposable {

	@Parameter
	private Context context;

	@Parameter
	private ObjectService objectService;

	@Parameter
	private CommandService commandService;

	private final Map<String, InteractiveLabeling> labelings = new HashMap<>();
	private final Map<InteractiveLabeling, List<BdvSource>> sources = new HashMap<>();

	private BdvHandlePanel bdvHandlePanel;

	private boolean mode3D = false;

	public LabelEditorBdvPanel() {
		initialize();
	}

	public LabelEditorBdvPanel(Context context) {
		context.inject(this);
		initialize();
	}

	private void initialize() {
		setMinimumSize(new Dimension(100, 100));
		setPreferredSize(new Dimension(500, 500));
		setLayout( new MigLayout("fill") );

		BdvOptions options = Bdv.options().accumulateProjectorFactory(LabelEditorAccumulateProjector.factory);
		if(!is3DMode()) {
			System.out.println("2D mode");
			bdvHandlePanel = new BdvHandlePanel(null, options.is2D());
		} else {
			System.out.println("3D mode");
			bdvHandlePanel = new BdvHandlePanel( null, options);
		}
		this.add( bdvHandlePanel.getViewerPanel(), "span, grow, push" );
	}

	public <L> InteractiveLabeling add(LabelEditorModel<L> model) {
		LabelEditorView<L> view = initView(model);
		LabelEditorController<L> control = initControl(model, view);
		InteractiveLabeling labeling = createAndRegisterInteractiveLabeling(model, view, control);
		ArrayList<BdvSource> sources = new ArrayList<>();
		sources.add(display(model.getData(), "data"));
		if(view.renderers().size() > 0)
			view.renderers().forEach(renderer -> sources.add(display(renderer.getOutput(), renderer.getName())));
		this.sources.put(labeling, sources);
		return labeling;
	}

	private <L> LabelEditorView<L> initView(LabelEditorModel<L> model) {
		LabelEditorView<L> view = createView(model);
		if(context != null) {
			context.inject(view);
		}
		addRenderers(view);
		return view;
	}

	protected <L> LabelEditorView<L> createView(LabelEditorModel<L> model) {
		return new DefaultLabelEditorView<>(model);
	}

	protected void addRenderers(LabelEditorView view) {
		view.addDefaultRenderers();
	}

	private <L> LabelEditorController<L> initControl(LabelEditorModel<L> model, LabelEditorView<L> view) {
		LabelEditorController<L> control = createController();
		if(context != null) {
			context.inject(control);
		}
		initController(model, view, control);
		return control;
	}

	protected <L> LabelEditorController<L> createController() {
		return new DefaultLabelEditorController<>();
	}

	private <L> void initController(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorController<L> control) {
		LabelEditorInterface<L> viewerInstance = new BdvInterface<>(bdvHandlePanel, view);
		control.init(model, view, viewerInstance);
		addBehaviours(control);
		control.interfaceInstance().set3DViewMode(is3DMode());
	}

	protected void addBehaviours(LabelEditorController controller) {
		controller.addDefaultBehaviours();
	}

	private BdvSource display(final RandomAccessibleInterval rai, String title) {
		if(rai == null) return null;
		final BdvSource source = BdvFunctions.show(
				rai,
				title,
				Bdv.options().addTo( bdvHandlePanel ) );
		source.setActive( true );
		return source;
	}

	private <L> InteractiveLabeling createAndRegisterInteractiveLabeling(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorController<L> control) {
		InteractiveLabeling labeling = new DefaultInteractiveLabeling(model, view, control);
		labelings.put(model.getName(), labeling);
		if(objectService != null) {
			objectService.addObject(labeling);
		}
		return labeling;
	}

	public Container get() {
		return this;
	}

	private boolean is3DMode() {
		return mode3D;
	}

	public void setMode3D(boolean set3D) {
		mode3D = set3D;
	}

	@Override
	public void dispose() {
		labelings.forEach((name,  labeling) -> labeling.view().dispose());
		if(bdvHandlePanel != null) bdvHandlePanel.close();
	}

	public List<BdvSource> getSources() {
		List<BdvSource> res = new ArrayList<>();
		sources.forEach((interactiveLabeling, bdvSources) -> res.addAll(bdvSources));
		return res;
	}
}
