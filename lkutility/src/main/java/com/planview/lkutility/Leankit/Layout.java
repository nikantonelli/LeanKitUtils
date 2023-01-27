package com.planview.lkutility.leankit;

import java.util.Arrays;

public class Layout {
	public Lane[] lanes;
	public String layoutChecksum;
	public Lane[] getLanes() {
		return lanes;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(lanes);
		result = prime * result + ((layoutChecksum == null) ? 0 : layoutChecksum.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Layout other = (Layout) obj;
		if (!Arrays.equals(lanes, other.lanes))
			return false;
		if (layoutChecksum == null) {
			if (other.layoutChecksum != null)
				return false;
		} else if (!layoutChecksum.equals(other.layoutChecksum))
			return false;
		return true;
	}
	public void setLanes(Lane[] lanes) {
		this.lanes = lanes;
	}
	public String getLayoutChecksum() {
		return layoutChecksum;
	}
	public void setLayoutChecksum(String checksum) {
		this.layoutChecksum = checksum;
	}
}
