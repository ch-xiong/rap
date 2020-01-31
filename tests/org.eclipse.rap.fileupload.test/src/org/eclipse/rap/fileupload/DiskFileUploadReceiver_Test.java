/*******************************************************************************
 * Copyright (c) 2011, 2018 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.fileupload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.rap.fileupload.internal.FileDetailsImpl;
import org.eclipse.rap.fileupload.test.FileUploadTestUtil;
import org.junit.After;
import org.junit.Test;


public class DiskFileUploadReceiver_Test {

  private File createdFile;
  private File createdContentTypeFile;

  @After
  public void tearDown() {
    if( createdFile != null ) {
      createdFile.delete();
      createdFile = null;
    }
    if ( createdContentTypeFile != null ) {
      createdContentTypeFile.delete();
      createdContentTypeFile = null;
    }
  }

  @Test
  public void testInitialGetTargetFiles() {
    DiskFileUploadReceiver receiver = new DiskFileUploadReceiver();

    assertEquals( 0, receiver.getTargetFiles().length );
  }

  @Test
  public void testCreateTargetFile() throws IOException {
    DiskFileUploadReceiver receiver = new DiskFileUploadReceiver();

    FileDetails details = new FileDetailsImpl( "foo.bar", "text/plain" );
    createdFile = receiver.createTargetFile( details );

    assertTrue( createdFile.exists() );
    assertEquals( "foo.bar", createdFile.getName() );
  }

  @Test
  public void testCreateContentTypeFile() throws IOException {
    DiskFileUploadReceiver receiver = new DiskFileUploadReceiver();

    FileDetails details = new FileDetailsImpl( "foo.bar", "text/plain" );
    createdFile = receiver.createTargetFile( details );
    createdContentTypeFile = receiver.createContentTypeFile( createdFile, details );

    assertTrue( createdContentTypeFile.exists() );
  }

  @Test
  public void testGetContentType() throws IOException {
    testReceive();
    assertEquals( "text/plain", DiskFileUploadReceiver.getContentType( createdFile ) );
    assertEquals( null, DiskFileUploadReceiver.getContentType( new File( "test" ) ) );
  }

  @Test
  public void testCreatedTargetFilesDiffer() throws IOException {
    DiskFileUploadReceiver receiver = new DiskFileUploadReceiver();

    FileDetails details = new FileDetailsImpl( "foo.bar", "text/plain" );
    createdFile = receiver.createTargetFile( details );
    File createdFile2 = receiver.createTargetFile( details );
    createdFile2.deleteOnExit();

    assertFalse( createdFile.getAbsolutePath().equals( createdFile2.getAbsolutePath() ) );
  }

  @Test
  public void testReceive() throws IOException {
    DiskFileUploadReceiver receiver = new DiskFileUploadReceiver();
    String content = "Hello world!";

    FileDetails details = new FileDetailsImpl( "foo.bar", "text/plain" );
    receiver.receive( new ByteArrayInputStream( content.getBytes() ), details );
    createdFile = receiver.getTargetFiles()[ 0 ];

    assertNotNull( createdFile );
    assertTrue( createdFile.exists() );
    assertEquals( content, FileUploadTestUtil.getFileContents( createdFile ) );
  }

  @Test
  public void testReceiveWithNullDetails() throws IOException {
    DiskFileUploadReceiver receiver = new DiskFileUploadReceiver();
    String content = "Hello world!";

    receiver.receive( new ByteArrayInputStream( content.getBytes() ), null );
    createdFile = receiver.getTargetFiles()[ 0 ];

    assertNotNull( createdFile );
    assertTrue( createdFile.exists() );
    assertEquals( "upload.tmp", createdFile.getName() );
    assertEquals( content, FileUploadTestUtil.getFileContents( createdFile ) );
  }

  @Test
  public void testSetUploadDirectory() {
    DiskFileUploadReceiver reciever = new DiskFileUploadReceiver();
    File file = new File( "myFile" );
    reciever.setUploadDirectory( file );
    File uploadDirectory = reciever.getUploadDirectory();
    assertSame( "Expected the file set to be returned", file, uploadDirectory );
  }

  @Test
  public void testInternalGetUploadDirectory() throws IOException {
    DiskFileUploadReceiver reciever = new DiskFileUploadReceiver();
    File file = new File( "myFile" );
    reciever.setUploadDirectory( file );
    File uploadDirectory = reciever.internalGetUploadDirectory();
    assertSame( "Expected the file set to be returned", file , uploadDirectory);
  }

  @Test
  public void testInternalGetUploadDirectory_null_file_set() throws IOException {
    DiskFileUploadReceiver reciever = new DiskFileUploadReceiver();
    reciever.setUploadDirectory( null );
    File uploadDirectory = reciever.internalGetUploadDirectory();
    assertNotNull( "Expected a temp directory set", uploadDirectory);
    assertTrue( "Expected a  directory returned", uploadDirectory.isDirectory());
  }

  @Test
  public void testInternalGetUploadDirectory_not_existing_directory_set() throws IOException {
    DiskFileUploadReceiver reciever = new DiskFileUploadReceiver();
    Path tempDirectory = Files.createTempDirectory( "_fileupload_test" );
    File targetFile = new File(tempDirectory.toFile(), "subdirectory_nonexisting");
    reciever.setUploadDirectory( targetFile );
    File uploadDirectory = reciever.internalGetUploadDirectory();
    assertNotNull( "Expected a temp directory set", uploadDirectory);
    assertTrue( "Expected a  directory returned", uploadDirectory.isDirectory());
  }

  @Test(expected = IOException.class)
  public void testInternalGetUploadDriecotry_file_is_set_as_target_Direcotry() throws IOException {
    DiskFileUploadReceiver reciever = new DiskFileUploadReceiver();
    Path tempDirectory = Files.createTempDirectory( "_fileupload_test" );
    File uploadFile = new File(tempDirectory.toFile(), "subdirectory_nonexisting");
    boolean createNewFile = uploadFile.createNewFile();
    assertTrue( "Failed to Create test file", createNewFile );
    reciever.setUploadDirectory( uploadFile );
    // This should throw the IOExcetion
    reciever.internalGetUploadDirectory();
  }

}
