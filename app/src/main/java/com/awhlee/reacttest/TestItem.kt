package com.awhlee.reacttest

class TestItem (var id: String, var subject: String, var description: String, var value: Int) {
    // Static functions are placed into companion objects
    companion object {
        fun prepareTestItems() : List<TestItem> {
            val returnList = ArrayList<TestItem>()
            returnList.add(TestItem("1f", "2e", "3d", 1))
            returnList.add(TestItem("3c", "4b", "6a",2 ))
            returnList.add(TestItem("1a", "2b", "3c", 3))
            returnList.add(TestItem("4d", "5e", "6f", 4))
            return returnList
        }
    }

    override  fun toString() : String {
        return "id: $id, subject: $subject, description: $description, value: $value\n"
    }
}


