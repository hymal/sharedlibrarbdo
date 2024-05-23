package main
import "fmt"func main() {
    var rgb = [3]string{"red", "green", "blue"}

    for _, color := range rgb {
        fmt.Println(color)
    }
}