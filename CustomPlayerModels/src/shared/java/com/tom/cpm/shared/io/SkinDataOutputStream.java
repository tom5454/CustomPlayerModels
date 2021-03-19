package com.tom.cpm.shared.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;

import com.tom.cpl.util.Image;

public class SkinDataOutputStream extends OutputStream {
	private Image img, template;
	private int block;
	private int channel;
	private int x, y;

	public SkinDataOutputStream(Image img, Image template, int channel) {
		this.img = img;
		this.template = template;
		this.channel = channel;
		this.block = -1;
		this.x = -1;
		int shift = 8 * (2 - channel);
		for(int y = 0;y<img.getHeight();y++) {
			for(int x = 0;x<img.getWidth();x++) {
				int t = template.getRGB(x, y);
				if((((t & (0xff << shift)) >>> shift) & 0xff) == 0xff) {
					if((t & 0xff) == 0xff)img.setRGB(x, y, 0xff000000);
					else img.setRGB(x, y, 0);
				}
			}
		}
	}

	@Override
	public void write(int b) throws IOException {
		if(block == -1 || block > 3) {
			if(!findNextBlock())throw new EOFException();
			block = 0;
		}
		int dt = img.getRGB(x, y);
		int shift = (block++) * 8;
		dt &= ~(0xff << shift);
		dt |= (b << shift);
		if((template.getRGB(x, y) & 0xff) == 0xff && block > 2) {
			block++;
			dt |= 0xff000000;
		}
		img.setRGB(x, y, dt);
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
}
