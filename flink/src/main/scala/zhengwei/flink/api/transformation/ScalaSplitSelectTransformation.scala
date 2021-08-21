package zhengwei.flink.api.transformation

/**
 * split 已经被标记过时，不再建议使用，
 * 建议使用 side output stream 和 filter，
 * 多流操作，将一个流分成多个流
 * <p>
 *  split -> select
 * </p>
 * <p>
 *  split stream -> data stream
 * </p>
 *
 * @author zhengwei AKA zenv
 * @since 2020/11/21 15:41
 */
object ScalaSplitSelectTransformation {
  def main(args: Array[String]): Unit = {

  }
}
