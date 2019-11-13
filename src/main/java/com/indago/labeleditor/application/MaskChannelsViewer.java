package com.indago.labeleditor.application;

import com.indago.labeleditor.core.LabelEditorPanel;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorTargetComponent;
import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.plugin.SciJavaPlugin;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Plugin(type = Command.class, name = "Mask channels viewer")
public class MaskChannelsViewer implements Command {

	@Parameter
	private ImgPlus data;

	@Parameter
	private int channelDim;

	@Parameter
	private int channelSource;

	@Parameter
	private OpService ops;
	
	@Parameter
	private Context context;

	@Override
	public void run() {
		printDims(data);
		ArrayImg<IntType, IntArray> backing = (ArrayImg<IntType, IntArray>) new ArrayImgFactory<>(new IntType()).create( Views.hyperSlice(data, channelDim, 0) );
//		Img<IntType> backing = new DiskCachedCellImgFactory<>(new IntType()).create(Views.hyperSlice(data, channelDim, 0)); // TODO why can I not do this?
		ImgLabeling< String, IntType > labeling = new ImgLabeling<>( backing );
		LabelEditorModel<String> model = new DefaultLabelEditorModel(labeling);
		ImgPlus dataImg = null;
		Map<Object, Integer> colors = new HashMap<>();
		Random random = new Random();
		for (int i = 0; i < data.dimension(channelDim); i++) {
			System.out.println(i);
			IntervalView slice = Views.hyperSlice(data, channelDim, i);
			if(i == channelSource) {
				dataImg = new ImgPlus(ops.convert().float32(slice));
			} else {
				Img bitSlice = ops.convert().bit(slice);
				ArrayImg<IntType, IntArray> backingSlice = (ArrayImg<IntType, IntArray>) new ArrayImgFactory<>(new IntType()).create( bitSlice );
				ImgLabeling< String, IntType > labelingSlice = new ImgLabeling<>( backingSlice );
				ops.labeling().cca(labelingSlice, bitSlice, ConnectedComponents.StructuringElement.EIGHT_CONNECTED, new LabelGenerator(i + "_"));
				Integer tag = i;
				int randomColor = ARGBType.rgba(random.nextInt(155) + 100, random.nextInt(155) + 100, random.nextInt(255) + 100, 100);
				colors.put(tag, randomColor);
				labelingSlice.getMapping().getLabels().forEach(labelset -> {
					model.tagging().addTag(tag, labelset);
				});
				ops.labeling().merge(labeling, labeling, labelingSlice);
			}
		}
		printDims(labeling);
		LabelEditorBdvPanel panel = new LabelEditorBdvPanel();
		context.inject(panel);
		panel.init(dataImg, model);
		colors.forEach((tag, color) -> panel.view().colors().get(tag).put(LabelEditorTargetComponent.FACE, color));
		JFrame frame = new JFrame();
		frame.setContentPane(panel.get());
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);
	}

	private void printDims(RandomAccessibleInterval data) {
		long[] dims = new long[data.numDimensions()];
		data.dimensions(dims);
		System.out.println(Arrays.toString(dims));
	}

	public static void main(String... args) throws IOException {
		ImageJ ij = new ImageJ();
		Img input = (Img) ij.io().open("/home/random/Development/imagej/project/3DAnalysisFIBSegmentation/High_glucose_Cell_1_complete-crop.tif");
//		Img input = (Img) ij.io().open("/home/random/Development/imagej/project/3DAnalysisFIBSegmentation/owncloud/Segmentation_masks/High_glucose/High_glucose_Cell_3_complete.tif");
		ij.command().run(MaskChannelsViewer.class, true, "data", input, "channelDim", 2, "channelSource", 2);
	}
}
