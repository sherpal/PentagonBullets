package io

import boopickle.CompositePickler
import sharednodejsapis.{Buffer, FileSystem, Path}

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import boopickle.Default._
import exceptions.DataNotFoundException
import globalvariables.{BaseDirectory, DataStorage}

import scala.scalajs.js.typedarray.TypedArrayBuffer
import scala.scalajs.js.typedarray.TypedArrayBufferOps._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Facility methods to read from and save to files.
  */
object IO {

  /**
    * Base directory of the electron package.json file.
    */
  val baseDirectory: String =
    DataStorage.retrieveGlobalValue("baseDirectory") match {
      case BaseDirectory(directory) =>
        directory
      case _ =>
        throw new DataNotFoundException
    }

  private def errToThrowable(err: Any): Throwable = err match {
    case err: Throwable => err
    case _              => js.JavaScriptException(err)
  }

  /**
    * Read file content.
    *
    * @param directory directory of the file, relative to baseDirectory
    * @return          the Future[Buffer] with the contents of the file.
    */
  def readFile(directory: String): Future[Buffer] = {
    val p = Promise[Buffer]()
    FileSystem.readFile(
      Path.join(baseDirectory, directory),
      (err: js.Error, data: js.UndefOr[Buffer]) => {
        if (err == null || js.isUndefined(err)) {
          p.success(data.get)
        } else {
          p.failure(errToThrowable(err))
        }
      }
    )
    p.future
  }

  /**
    * Read file content and return a deserialized FileContent in case of success.
    *
    * @param directory directory of the file, relative to baseDirectory
    * @return          the Future[FileContent] with the de-serialized FileContent
    */
  def readFileContent(directory: String): Future[FileContent] =
    for (data <- readFile(directory)) yield {
      Unpickle[FileContent](FileContent.fileContentPickler)
        .fromBytes(TypedArrayBuffer.wrap(data.buffer))
    }

  /**
    * Serialize the FileContent object, and register it into the specified directory.
    *
    * @param directory     directory to write the data to, relative to baseDirectory.
    * @param fileContent   the content to write.
    * @param errorCallback function called if error occured.
    */
  def writeFileContent(
      directory: String,
      fileContent: FileContent,
      errorCallback: (Throwable) => Any = (_) => {}
  ): Unit =
    try {
      implicit val pickler: CompositePickler[FileContent] = FileContent.fileContentPickler
      val serialized                                      = Pickle.intoBytes(fileContent)
      val buffer                                          = Buffer.from(serialized.arrayBuffer())
      FileSystem.writeFile(Path.join(baseDirectory, directory), buffer, (err: js.Error) => {
        if (err != null && !js.isUndefined(err)) {
          errorCallback(errToThrowable(err))
        }
      })
    } catch {
      case err: Throwable => errorCallback(err)
    }

  def open(directory: String, flags: String = "r"): Future[Int] = {
    val p = Promise[Int]()
    FileSystem.open(
      Path.join(baseDirectory, directory),
      flags,
      (err: js.Error, fd: js.UndefOr[Int]) => {
        if (err != null && !js.isUndefined(err)) {
          p.failure(errToThrowable(err))
        } else {
          p.success(fd.get)
        }
      }
    )
    p.future
  }

  def close(fd: Int): Future[Boolean] = {
    val p = Promise[Boolean]()
    FileSystem.close(fd, (err: js.Error) => {
      if (err != null && !js.isUndefined(err)) {
        p.failure(errToThrowable(err))
      } else {
        p.success(true)
      }
    })
    p.future
  }

  def mkdir(directory: String, callback: (Throwable) => Any = (_) => {}): Future[Boolean] = {
    val p = Promise[Boolean]()
    FileSystem.mkdir(Path.join(baseDirectory, directory), (err: js.Error) => {
      if (err != null && !js.isUndefined(err)) {
        p.failure(errToThrowable(err))
      } else {
        p.success(true)
      }
    })
    p.future
  }

  def delete(directory: String, callback: (Throwable) => Any = (_) => {}): Future[Boolean] = {
    val p = Promise[Boolean]()
    FileSystem.unlink(Path.join(baseDirectory, directory), (err: js.Error) => {
      if (err != null && !js.isUndefined(err)) {
        p.failure(errToThrowable(err))
      } else {
        p.success(true)
      }
    })
    p.future
  }

}
