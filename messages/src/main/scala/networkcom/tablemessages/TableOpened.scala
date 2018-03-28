package networkcom.tablemessages

/**
 * Response from the server to the OpenTable message.
 * @param name         the name of the new table, which will be the name of the game
 * @param success      whether the table was actually created
 * @param errorMessage if success is false, returning an error message. Typically this can happen either because the
 *                     name already exists, or there is already too many tables opened (although unlikely to happen)
 */
final case class TableOpened(name: String, success: Boolean, errorMessage: String) extends TableServerMessages