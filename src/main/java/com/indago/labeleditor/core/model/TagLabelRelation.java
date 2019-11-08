package com.indago.labeleditor.core.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TagLabelRelation<L> {

	Map<L, Set<Object>> get();

	void addTag(Object tag, L label);
	void removeTag(Object tag, L label);
	Set<Object> getTags(L label);
	void removeTag(Object tag);
	List<L> getLabels(LabelEditorTag tag);

}