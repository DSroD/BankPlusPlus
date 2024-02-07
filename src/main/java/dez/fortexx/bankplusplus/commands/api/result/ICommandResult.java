package dez.fortexx.bankplusplus.commands.api.result;

public sealed interface ICommandResult permits BaseComponentResult, ErrorResult, InvalidCommandSenderResult, InvalidUsageResult, SuccessResult {

}
