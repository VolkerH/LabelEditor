package com.indago.labeleditor.plugin.bvv;

import bvv.util.Bvv;
import bvv.util.BvvFunctions;
import bvv.util.BvvHandle;
import bvv.util.BvvSource;
import bvv.util.BvvStackSource;
import com.indago.labeleditor.core.AbstractLabelEditorPanel;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.controller.LabelEditorInterface;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.type.numeric.ARGBType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LabelEditorBvvPanel<L> extends AbstractLabelEditorPanel<L> {

	private BvvHandle bvvHandle;
	private List< BvvStackSource > bvvSources;
	private LabelEditorInterface<L> viewerInstance;

	@Override
	protected void initController() {
		viewerInstance = new BvvInterface<>(bvvHandle, bvvSources);
		controller.init(viewerInstance, model(), view());
		addActionHandlers(controller);
		controller.set3DViewMode(mode3D);
	}

	@Override
	protected Component buildInterface() {
		bvvSources = new ArrayList<>();
//		BvvOptions options = Bvv.options().accumulateProjectorFactory(LabelEditorAccumulateProjector.factory);
		BvvStackSource<ARGBType> source1 = BvvFunctions.show(fakeImg(), "", Bvv.options());
		bvvSources.add(source1);
		bvvHandle = source1.getBvvHandle();
		return bvvHandle.getViewerPanel();
	}

	private ImgPlus<ARGBType> fakeImg() {
		return new ImgPlus<>(new DiskCachedCellImgFactory<>(new ARGBType()).create(model.labels()));
	}

	@Override
	protected void displayLabeling() {
		if(view().renderers().size() == 0) return;

		//TODO make virtual channels work
//		List<LUTChannel> virtualChannels = renderer.getVirtualChannels();
//		if(virtualChannels != null) {
//			List<BdvVirtualChannelSource> sources = BdvFunctions.show(
//					labelColorImg,
//					virtualChannels,
//					"solution",
//					Bdv.options().addTo(bdvGetHandlePanel()));
//			final Bdv bdv = sources.get( 0 );
//			for (int i = 0; i < virtualChannels.size(); ++i ) {
//				virtualChannels.get( i ).setPlaceHolderOverlayInfo( sources.get( i ).getPlaceHolderOverlayInfo() );
//				virtualChannels.get( i ).setViewerPanel( bdv.getBdvHandle().getViewerPanel() );
//			}
//		} else {
		view().renderers().forEach(renderer -> displayInBvv(renderer.getOutput(), renderer.getName()));
//		}
	}

	@Override
	protected void displayData() {
		if(data != null) {
			displayInBvv( data, "RAW" );
		}
	}

	private void displayInBvv(final RandomAccessibleInterval img,
	                          final String title ) {
		final BvvStackSource source = BvvFunctions.show(
				img,
				title,
				Bvv.options().addTo(bvvHandle) );
		getSources().add( source );
		source.setActive( true );
	}

	@Override
	protected void clearInterface() {
		for ( final BvvSource source : getSources()) {
			source.removeFromBdv();
		}
		getSources().clear();
	}

	@Override
	public BvvHandle getViewerHandle() {
		return bvvHandle;
	}

	public List< BvvStackSource > getSources() {
		return bvvSources;
	}

	@Override
	protected void addActionHandlers(LabelEditorController<L> actionManager) {
		actionManager.init(viewerInstance, model(), view());
		actionManager.addDefaultActionHandlers();
	}

	@Override
	public void dispose() {
		if(getViewerHandle() != null) getViewerHandle().close();
	}

	@Override
	public void updateData(ImgPlus<L> imgPlus) {
		super.setData(imgPlus);
	}

}
