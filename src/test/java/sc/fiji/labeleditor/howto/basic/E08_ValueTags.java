package sc.fiji.labeleditor.howto.basic;

import net.imagej.ImageJ;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.ui.UIService;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorValueTag;
import sc.fiji.labeleditor.core.view.LabelEditorTargetComponent;

import java.io.IOException;
import java.util.Random;

/**
 * How to assign value tags in the LabelEditor
 */
public class E08_ValueTags {

	public void run() throws IOException {

		ImageJ ij = new ImageJ();
		ij.launch();

		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());

		Img<IntType> binary = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(binary, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		LabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling);
		model.setData(input);

		Random random = new Random();
		labeling.getMapping().getLabels().forEach(label -> {

			//for each label, assign a tag with a random value between 0 and 100

			LabelEditorValueTag randomValue = new LabelEditorValueTag<>("random", new IntType(random.nextInt(100)));

			model.tagging().addTagToLabel(randomValue, label);

			// now assign a color range to this value tag
			model.colors().get(randomValue.getIdentifier()).put(
					LabelEditorTargetComponent.FACE,
					ARGBType.rgba(0,0,255,250),
					ARGBType.rgba(255,0,0,250),
					new IntType(0), new IntType(100)
			);

		});

		ij.ui().show(model);
	}

	public static void main(String... args) throws IOException {
		new E08_ValueTags().run();
	}

}