/**
 * Copyright © 2015, University of Washington
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     * Neither the name of the University of Washington nor the names
 *       of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written
 *       permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL UNIVERSITY OF
 * WASHINGTON BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
#include "edu_uw_apl_commons_tsk4j_volsys_Partition.h"

#include <tsk/libtsk.h>


/**
 * @author Stuart Maclean
 *
 * Implementations for volsys.Partition native methods.
 */

/*
 * Class:     edu_uw_apl_commons_tsk4j_volsys_Partition
 * Method:    address
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL 
Java_edu_uw_apl_commons_tsk4j_volsys_Partition_address
(JNIEnv *env, jobject thiz, jlong nativePtr ) {

  TSK_VS_PART_INFO* info = (TSK_VS_PART_INFO*)nativePtr;
  return (jlong)info->addr;
}

/*
 * Class:     edu_uw_apl_commons_tsk4j_volsys_Partition
 * Method:    description
 * Signature: (J)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL 
Java_edu_uw_apl_commons_tsk4j_volsys_Partition_description
(JNIEnv *env, jobject thiz, jlong nativePtr ) {
  
  TSK_VS_PART_INFO* info = (TSK_VS_PART_INFO*)nativePtr;
  return (*env)->NewStringUTF( env, info->desc );
}


/*
 * Class:     edu_uw_apl_commons_tsk4j_volsys_Partition
 * Method:    flags
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_edu_uw_apl_commons_tsk4j_volsys_Partition_flags
(JNIEnv *env, jobject thiz, jlong nativePtr ) {

  TSK_VS_PART_INFO* info = (TSK_VS_PART_INFO*)nativePtr;
  return (jint)info->flags;
}

/*
 * Class:     edu_uw_apl_commons_tsk4j_volsys_Partition
 * Method:    length
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL 
Java_edu_uw_apl_commons_tsk4j_volsys_Partition_length
(JNIEnv *env, jobject thiz, jlong nativePtr ) {

  TSK_VS_PART_INFO* info = (TSK_VS_PART_INFO*)nativePtr;
  return (jlong)info->len;
}
/*
 * Class:     edu_uw_apl_commons_tsk4j_volsys_Partition
 * Method:    start
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL 
Java_edu_uw_apl_commons_tsk4j_volsys_Partition_start
(JNIEnv *env, jobject thiz, jlong nativePtr ) {

  TSK_VS_PART_INFO* info = (TSK_VS_PART_INFO*)nativePtr;
  return (jlong)info->start;
}

/*
 * Class:     edu_uw_apl_commons_tsk4j_volsys_Partition
 * Method:    slot
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_edu_uw_apl_commons_tsk4j_volsys_Partition_slot
(JNIEnv *env, jobject thiz, jlong nativePtr ) {

  TSK_VS_PART_INFO* info = (TSK_VS_PART_INFO*)nativePtr;
  // slot_num described as 'int8_t' in the TSK api-docs...
  return (jint)(info->slot_num);
}

/*
 * Class:     edu_uw_apl_commons_tsk4j_volsys_Partition
 * Method:    table
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_edu_uw_apl_commons_tsk4j_volsys_Partition_table
(JNIEnv *env, jobject thiz, jlong nativePtr ) {

  TSK_VS_PART_INFO* info = (TSK_VS_PART_INFO*)nativePtr;
  // table_num described as 'int8_t' in the TSK api-docs...
  return (jint)(info->table_num);
}

/*
 * Class:     edu_uw_apl_commons_tsk4j_volsys_Partition
 * Method:    read
 * Signature: (JJ[BII)I
 */
JNIEXPORT jint JNICALL 
Java_edu_uw_apl_commons_tsk4j_volsys_Partition_read
(JNIEnv *env, jobject thiz, jlong nativePtr, jlong volumeOffset, 
 jbyteArray buf, jint bufOffset, jint len ) {

#ifdef TSKDEBUG
  fprintf( stderr,
		   "JNI: info %lx offset %ld len %d\n", nativePtr, volumeOffset, len );
#endif

  TSK_VS_PART_INFO* info = (TSK_VS_PART_INFO*)nativePtr;

  /*
	We have seen the tsk_vs_part_read (to 4.1 at least) spin/hang 
	if len supplied is larger than remaining bytes in partition, so check.
	We prefer the check here and not in the Java since we then keep the
	check as close to the underlying native call as possible
  */
  jlong size = info->len * info->vs->block_size;
  jlong remaining = size - volumeOffset;
  size_t a_len = remaining < len ? remaining : len;

#ifdef TSKDEBUG
  fprintf( stderr,
		   "JNI: info %lx offset %ld a_len %d\n", 
		   nativePtr, volumeOffset, (int)a_len );
#endif

  char* bufC = (char*)malloc( len );
  ssize_t read = tsk_vs_part_read( info, volumeOffset, bufC, a_len );
  if( read != -1 ) 
	(*env)->SetByteArrayRegion( env, buf, bufOffset, read, (const jbyte*)bufC );
  free( bufC );
  return (jint)read;
}

// eof
