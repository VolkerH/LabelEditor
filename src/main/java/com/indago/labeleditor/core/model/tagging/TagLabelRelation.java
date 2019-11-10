package com.indago.labeleditor.core.model.tagging;

import org.scijava.listeners.Listeners;

import java.util.List;
import java.util.Map;
import java.util.Set;

//TODO is this interface needed?
public interface TagLabelRelation<L> {

	Map<L, Set<Object>> get();
	void addTag(Object tag, L label);
	void removeTag(Object tag, L label);
	Set<Object> getTags(L label);
	void removeTag(Object tag);
	List<L> getLabels(LabelEditorTag tag);
	Listeners< TagChangeListener > listeners();

}