package com.tom.cpm.shared.io;

import java.io.InputStream;

import com.tom.cpl.util.Image;

public class SkinDataInputStream extends InputStream {
	private Image img, template;
	private int block;
	private int channel;
	private int x, y;

	public SkinDataInputStream(Image img, Image template, int channel) {
		this.img = img;
		this.template = template;
		this.channel = channel;
		this.block = -1;
		this.x = -1;
	}

	@Override
	public int read() {
		if(block == -1 || block > 3) {
			if(!findNextBlock())return -1;
			block = 0;
		}
		int dt = img.getRGB(x, y);
		int shift = (block++) * 8;
		if((template.getRGB(x, y) & 0xff) == 0xff && block > 2) {
			block++;
		}
		return ((dt & (0xff << shift)) >>> shift) & 0xff;
	}

	private boolean findNextBlock() {
		int shift = 8 * (2 - channel);
		for(int y = this.y;y<img.getHeight();y++) {
			for(int x = this.x + 1;x<img.getWidth();x++) {
				int t = template.getRGB(x, y);
				if((((t & (0xff << shift)) >>> shift) & 0xff) == 0xff) {
					this.x = x;
					this.y = y;
					return true;
				}
			}
			this.x = -1;
		}
		return false;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public int getChannel() {
		return channel;
	}

	@Override
	public void close() {}
}
