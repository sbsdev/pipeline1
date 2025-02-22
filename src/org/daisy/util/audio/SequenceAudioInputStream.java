package org.daisy.util.audio;

/*
 *	SequenceAudioInputStream.java
 *
 *	This file is part of jsresources.org
 *
 *  Modified by mgylling 200709, implemented close(), generics
 */

/*
 * Copyright (c) 1999 - 2001 by Matthias Pfisterer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
 |<---            this code is formatted to fit into 80 columns             --->|
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class SequenceAudioInputStream extends AudioInputStream {
	private List<AudioInputStream> m_audioInputStreamList;
	private int m_nCurrentStream;

	public SequenceAudioInputStream(
			AudioFormat audioFormat,
			Collection<AudioInputStream> audioInputStreams) {
		super(new ByteArrayInputStream(new byte[0]), audioFormat, AudioSystem.NOT_SPECIFIED);
		m_audioInputStreamList = new ArrayList<AudioInputStream>(audioInputStreams);
		m_nCurrentStream = 0;
	}
	private AudioInputStream getCurrentStream() {
		return m_audioInputStreamList.get(m_nCurrentStream);
	}

	private boolean advanceStream() {
		m_nCurrentStream++;
		boolean bAnotherStreamAvailable = (m_nCurrentStream < m_audioInputStreamList.size());
		return bAnotherStreamAvailable;
	}

	public long getFrameLength() {
		long lLengthInFrames = 0;
		Iterator<AudioInputStream> streamIterator = m_audioInputStreamList.iterator();
		while (streamIterator.hasNext()) {
			AudioInputStream stream = streamIterator.next();
			long lLength = stream.getFrameLength();
			if (lLength == AudioSystem.NOT_SPECIFIED) {
				return AudioSystem.NOT_SPECIFIED;
			}
			lLengthInFrames += lLength;
		}
		return lLengthInFrames;
	}

	public int read() throws IOException {
		AudioInputStream stream = getCurrentStream();
		int nByte = stream.read();
		if (nByte == -1) {
			/*
			  The end of the current stream has been signaled.
			  We try to advance to the next stream.
			 */
			boolean bAnotherStreamAvailable = advanceStream();
			if (bAnotherStreamAvailable) {
				/*
				  There is another stream. We recurse into this method
				  to read from it.
				 */
				return read();
			}
			/*
			  No more data. We signal EOF.
			 */
			return -1;
		}
		/*
		  The most common case: We return the byte.
		 */
		return nByte;
	}

	public int read(byte[] abData, int nOffset,
			int nLength) throws IOException {
		AudioInputStream stream = getCurrentStream();
		int nBytesRead = stream.read(abData, nOffset, nLength);
		if (nBytesRead == -1) {
			/*
			  The end of the current stream has been signaled.
			  We try to advance to the next stream.
			 */
			boolean bAnotherStreamAvailable = advanceStream();
			if (bAnotherStreamAvailable) {
				/*
				  There is another stream. We recurse into this method
				  to read from it.
				 */
				return read(abData, nOffset, nLength);
			}
			/*
			  No more data. We signal EOF.
			 */
			return -1;
		}
		/*
		  The most common case: We return the length.
		 */
		return nBytesRead;
	}

	public long skip(@SuppressWarnings("unused")long lLength) throws IOException {
		throw new IOException("skip() is not implemented in class SequenceInputStream. Mail if you need this feature.");
	}

	public int available() throws IOException {
		return getCurrentStream().available();
	}

	public void close() throws IOException {
		for(AudioInputStream ais : m_audioInputStreamList) {
			ais.close();
		}
	}

	public void mark(@SuppressWarnings("unused")int nReadLimit) {
		throw new RuntimeException("mark() is not implemented in class SequenceInputStream. Mail if you need this feature.");
	}

	public void reset() throws IOException {
		throw new IOException("reset() is not implemented in class SequenceInputStream. Mail if you need this feature.");
	}

	public boolean markSupported() {
		return false;
	}

}

/*** SequenceAudioInputStream.java ***/

