package com.horizen.chain

import java.io._

import com.horizen.fixtures.SidechainBlockInfoFixture
import com.horizen.utils.{BytesUtils, WithdrawalEpochInfo}
import org.junit.Assert.{assertEquals, assertTrue}
import org.junit.Test
import org.scalatest.junit.JUnitSuite
import scorex.core.block.Block
import scorex.core.consensus.ModifierSemanticValidity
import scorex.util.{ModifierId, bytesToId, idToBytes}

class SidechainBlockInfoTest extends JUnitSuite with SidechainBlockInfoFixture {
  setSeed(1000L)

  val height = 100
  val score: Long = 1L << 32 + 1
  val parentId: ModifierId = getRandomModifier()
  val timestamp: Block.Timestamp = 567081L
  val semanticValidity: ModifierSemanticValidity = ModifierSemanticValidity.Valid
  val refIds = Seq("0269861FB647BA5730425C79AC164F8A0E4003CF30990628D52CEE50DFEC9213", "E78283E4B2A92784F252327374D6D587D0A4067373AABB537485812671645B70",
    "77B57DC4C97CD30AABAA00722B0354BE59AB74397177EA1E2A537991B39C7508").map(hex => byteArrayToMainchainBlockReferenceId(BytesUtils.fromHexString(hex)))
  val withdrawalEpochInfo: WithdrawalEpochInfo = WithdrawalEpochInfo(10, 100)

  @Test
  def creation(): Unit = {
    val clonedParentId: ModifierId = bytesToId(idToBytes(parentId))
    val info: SidechainBlockInfo = SidechainBlockInfo(height, score, parentId, timestamp, semanticValidity, refIds, withdrawalEpochInfo)

    assertEquals("SidechainBlockInfo height is different", height, info.height)
    assertEquals("SidechainBlockInfo score is different", score, info.score)
    assertEquals("SidechainBlockInfo parentId is different", clonedParentId, info.parentId)
    assertEquals("SidechainBlockInfo timestamp is different", timestamp, info.timestamp)
    assertEquals("SidechainBlockInfo semanticValidity is different", semanticValidity, info.semanticValidity)
    assertEquals("SidechainBlockInfo mainchain lock reference size is different", refIds.length, info.mainchainBlockReferenceHashes.length)
    refIds.zipWithIndex.foreach{case (ref, index) =>
      assertEquals("SidechainBlockInfo reference is different", ref, info.mainchainBlockReferenceHashes(index))
    }
    assertEquals("SidechainBlockInfo withdrawalEpochInfo is different", withdrawalEpochInfo, info.withdrawalEpochInfo)
  }

  @Test
  def serialization(): Unit = {
    val info: SidechainBlockInfo = SidechainBlockInfo(height, score, parentId, timestamp, semanticValidity, refIds, withdrawalEpochInfo)
    val bytes = info.bytes


    // Test 1: try to deserializer valid bytes
    val serializedInfoTry = SidechainBlockInfoSerializer.parseBytesTry(bytes)

    assertTrue("SidechainBlockInfo expected to by parsed.", serializedInfoTry.isSuccess)
    assertEquals("SidechainBlockInfo height is different", info.height, serializedInfoTry.get.height)
    assertEquals("SidechainBlockInfo score is different", info.score, serializedInfoTry.get.score)
    assertEquals("SidechainBlockInfo timestamp is different", info.timestamp, serializedInfoTry.get.timestamp)
    assertEquals("SidechainBlockInfo parentId is different", info.parentId, serializedInfoTry.get.parentId)
    assertEquals("SidechainBlockInfo semanticValidity is different", info.semanticValidity, serializedInfoTry.get.semanticValidity)
    val references = serializedInfoTry.get.mainchainBlockReferenceHashes
    assertEquals("Size of mainchain references shall be the same", info.mainchainBlockReferenceHashes.size, references.size)
    for(index <- refIds.indices)
      assertEquals("SidechainBlockInfo reference is different", info.mainchainBlockReferenceHashes(index), references(index))
    assertEquals("SidechainBlockInfo withdrawalEpochInfo is different", info.withdrawalEpochInfo, serializedInfoTry.get.withdrawalEpochInfo)


//    Uncomment and run if you want to update regression data.
//    val out = new BufferedWriter(new FileWriter("src/test/resources/sidechainblockinfo_hex"))
//    out.write(BytesUtils.toHexString(bytes))
//    out.close()



    // Test 2: try to deserialize broken bytes.
    assertTrue("SidechainBlockInfo expected to be not parsed due to broken data.", SidechainBlockInfoSerializer.parseBytesTry("broken bytes".getBytes).isFailure)
  }

  @Test
  def serialization_regression(): Unit = {
    var bytes: Array[Byte] = null
    try {
      val classLoader = getClass.getClassLoader
      val file = new FileReader(classLoader.getResource("sidechainblockinfo_hex").getFile)
      bytes = BytesUtils.fromHexString(new BufferedReader(file).readLine())
    }
    catch {
      case e: Exception =>
        assertEquals(e.toString, true, false)
    }

    val serializedInfoTry = SidechainBlockInfoSerializer.parseBytesTry(bytes)
    assertTrue("SidechainBlockInfo expected to by parsed.", serializedInfoTry.isSuccess)
    assertEquals("SidechainBlockInfo height is different", height, serializedInfoTry.get.height)
    assertEquals("SidechainBlockInfo score is different", score, serializedInfoTry.get.score)
    assertEquals("SidechainBlockInfo parentId is different", parentId, serializedInfoTry.get.parentId)
    assertEquals("SidechainBlockInfo semanticValidity is different", semanticValidity, serializedInfoTry.get.semanticValidity)
    val references = serializedInfoTry.get.mainchainBlockReferenceHashes
    assertEquals("Size of mainchain references shall be the same", refIds.size, references.size)
    for(index <- refIds.indices)
      assertEquals("SidechainBlockInfo reference is different", refIds(index), references(index))
    assertEquals("SidechainBlockInfo withdrawalEpochInfo is different", withdrawalEpochInfo, serializedInfoTry.get.withdrawalEpochInfo)
  }
}
