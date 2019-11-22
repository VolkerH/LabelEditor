#@ OpService ops
#@ IOService io
#@ UIService ui

import net.imglib2.algorithm.labeling.ConnectedComponents.StructuringElement
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel

input = io.open("https://samples.fiji.sc/blobs.png")

binary = ops.threshold().otsu(input)

labeling = ops.labeling().cca(binary, StructuringElement.EIGHT_CONNECTED)

model = new DefaultLabelEditorModel(labeling, input)

ui.show(model)