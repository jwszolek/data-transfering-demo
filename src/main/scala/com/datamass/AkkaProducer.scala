package com.datamass

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import com.datamass.TransSys.{Pay, WhoToGreet}
import scala.util.{Failure, Success}
import scala.concurrent.Future


//#TransSys-companion
object TransSys {
  //#greeter-messages
  def props(message: String, printerActor: ActorRef): Props = Props(new TransSys(message, printerActor))
  //#greeter-messages
  final case class WhoToGreet(who: String)
  case object Pay
}
//#TransSys-companion

class TransSys(message: String, printerActor: ActorRef) extends Actor {
  import TransSys._
  import Printer._
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  import system.dispatcher

  var greeting = ""

  def receive = {
    case WhoToGreet(who) => {

      val rawMsg = """{
                      |  "operation": "create",
                      |  "payload": {
                      |    "TableName": "purchased-items",
                      |    "Item": {
                      |      "id": "1234ABC1",
                      |      "number": 24,
                      |      "cat": "general",
                      |      "price": 1500,
                      |      "shop": "shopname",
                      |       "dt":0
                      |    }
                      |  }
                      |}""".replaceAll("\\|","").replaceAll("\\n","")


      val possibleCategories = Seq(
        "Car-accessories",
        "General",
        "Electronics",
        "Clothes",
        "Jewelry"
      )

      val category = possibleCategories(
        scala.util.Random.nextInt(possibleCategories.length)
      )

      val randomPrice = scala.util.Random
      val finalMsg = rawMsg.replace("1500",randomPrice.nextInt(2500).toString).replace("general",category)
        .replace("24",scala.util.Random.nextInt(10).toString).replace("shopname",who)

      val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(
        method = HttpMethods.POST,
        uri = "https://3sqbgahxsl.execute-api.eu-west-1.amazonaws.com/dev-env",
        entity = HttpEntity(ContentTypes.`application/json`, finalMsg)
      ))

      responseFuture
        .onComplete {
          case Success(res) => println(res)
          case Failure(_)   => sys.error("something wrong")
        }

      greeting = message + ", " + who
    }
    case Pay           =>
      printerActor ! Greeting(greeting)
  }
}


object Printer {
  //#printer-messages
  def props: Props = Props[Printer]
  //#printer-messages
  final case class Greeting(greeting: String)
}

class Printer extends Actor with ActorLogging {
  import Printer._

  def receive = {
    case Greeting(greeting) =>
      log.info("Greeting received (from " + sender() + "): " + greeting)
  }
}

object AkkaQuickstart extends App {
  import TransSys._


  val system: ActorSystem = ActorSystem("helloAkka")
  val printer: ActorRef = system.actorOf(Printer.props, "printerActor")

  val webStore1: ActorRef =
    system.actorOf(TransSys.props("Howdy ", printer), "webStore1")

  val webStore2: ActorRef =
    system.actorOf(TransSys.props("Howdy ", printer), "webStore2")

  val webStore3: ActorRef =
    system.actorOf(TransSys.props("Howdy ", printer), "webStore3")

  val webStore4: ActorRef =
    system.actorOf(TransSys.props("Howdy ", printer), "webStore4")

  val webStore5: ActorRef =
    system.actorOf(TransSys.props("Howdy ", printer), "webStore5")



  while(true) {

    webStore1 ! WhoToGreet("WebStore1")
    webStore1 ! Pay

    webStore2 ! WhoToGreet("WebStore2")
    webStore2 ! Pay

    webStore3 ! WhoToGreet("WebStore3")
    webStore3 ! Pay

    webStore4 ! WhoToGreet("WebStore4")
    webStore4 ! Pay

    webStore5 ! WhoToGreet("WebStore5")
    webStore5 ! Pay

    Thread.sleep(5000)
  }

}
